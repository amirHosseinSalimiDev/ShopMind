package com.shopmind.recommendation.ml

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class LiteRTRecommendationEngineTest {

    private var engine: LiteRTRecommendationEngine? = null

    @Before
    fun setUp() {
        val modelFile = File("src/main/assets/recommendation_model.tflite").let {
            if (it.exists()) it else File("app/src/main/assets/recommendation_model.tflite")
        }
        assumeTrue("recommendation_model.tflite must exist", modelFile.exists())

        try {
            val bytes = modelFile.readBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.order(ByteOrder.nativeOrder())
            buffer.put(bytes)
            buffer.rewind()

            engine = LiteRTRecommendationEngine.createFromBuffer(buffer)
        } catch (_: UnsatisfiedLinkError) {
            // Native LiteRT library (.so) is target-built for Android OS and not loaded in JVM host test.
            engine = null
        }
    }

    @Test
    fun testInferenceWithSampleVectors() = runBlocking {
        val activeEngine = engine ?: run {
            println("Skipping host JVM native inference test (native LiteRt lib requires Android runtime)")
            return@runBlocking
        }

        val userProfile = FloatArray(229) { 0.1f }
        val candidateProduct = FloatArray(229) { 0.2f }

        val score = activeEngine.predictRelevanceScore(userProfile, candidateProduct)
        assertTrue(!score.isNaN())
        assertTrue(!score.isInfinite())
        assertTrue(score in 0.0f..1.0f)
    }

    @Test
    fun testBatchInference() = runBlocking {
        val activeEngine = engine ?: return@runBlocking

        val userProfile = FloatArray(229) { 0.1f }
        val candidates = listOf(
            FloatArray(229) { 0.1f },
            FloatArray(229) { 0.5f },
            FloatArray(229) { 0.8f }
        )

        val scores = activeEngine.predictBatch(userProfile, candidates)
        assertEquals(3, scores.size)
        for (score in scores) {
            assertTrue(score in 0.0f..1.0f)
        }
    }

    @Test
    fun testCloseLifecycle() {
        val activeEngine = engine ?: return
        activeEngine.close()
        try {
            runBlocking {
                activeEngine.predictRelevanceScore(FloatArray(229), FloatArray(229))
            }
            fail("Expected IllegalStateException after close")
        } catch (_: IllegalStateException) {
            // Expected
        }
    }
}
