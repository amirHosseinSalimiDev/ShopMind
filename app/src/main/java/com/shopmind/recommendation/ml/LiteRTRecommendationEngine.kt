package com.shopmind.recommendation.ml

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class LiteRTRecommendationEngine(
    private val interpreter: Interpreter
) : Closeable {

    private val mutex = Mutex()
    @Volatile
    private var isClosed = false

    init {
        validateInterpreter(interpreter)
    }

    private fun validateInterpreter(interp: Interpreter) {
        require(interp.inputTensorCount > 0) { "Model has no input tensors" }
        require(interp.outputTensorCount > 0) { "Model has no output tensors" }

        val inputTensor = interp.getInputTensor(0)
        val outputTensor = interp.getOutputTensor(0)

        val inputShape = inputTensor.shape()
        require(inputShape.size == 2 && inputShape[1] == 458) {
            "Expected input tensor dimension [*, 458], got ${inputShape.joinToString()}"
        }
        require(inputTensor.dataType() == DataType.FLOAT32) {
            "Expected input data type FLOAT32, got ${inputTensor.dataType()}"
        }
        require(outputTensor.dataType() == DataType.FLOAT32) {
            "Expected output data type FLOAT32, got ${outputTensor.dataType()}"
        }
    }

    suspend fun predictRelevanceScore(userProfile: FloatArray, candidateProduct: FloatArray): Float {
        require(userProfile.size == 229) { "User profile must have 229 elements" }
        require(candidateProduct.size == 229) { "Candidate product vector must have 229 elements" }

        val input = FloatArray(458)
        userProfile.copyInto(input, destinationOffset = 0)
        candidateProduct.copyInto(input, destinationOffset = 229)

        val inputBatch = arrayOf(input)
        val outputBatch = arrayOf(floatArrayOf(0f))

        mutex.withLock {
            check(!isClosed) { "LiteRTRecommendationEngine is closed" }
            interpreter.run(inputBatch, outputBatch)
        }

        return outputBatch[0][0]
    }

    suspend fun predictBatch(userProfile: FloatArray, candidateProducts: List<FloatArray>): FloatArray {
        require(userProfile.size == 229) { "User profile must have 229 elements" }
        if (candidateProducts.isEmpty()) return floatArrayOf()

        val scores = FloatArray(candidateProducts.size)

        mutex.withLock {
            check(!isClosed) { "LiteRTRecommendationEngine is closed" }
            for (i in candidateProducts.indices) {
                val candidate = candidateProducts[i]
                require(candidate.size == 229) { "Candidate vector at $i must have 229 elements" }

                val input = FloatArray(458)
                userProfile.copyInto(input, destinationOffset = 0)
                candidate.copyInto(input, destinationOffset = 229)

                val inputBatch = arrayOf(input)
                val outputBatch = arrayOf(floatArrayOf(0f))

                interpreter.run(inputBatch, outputBatch)
                scores[i] = outputBatch[0][0]
            }
        }

        return scores
    }

    override fun close() {
        synchronized(this) {
            if (!isClosed) {
                isClosed = true
                interpreter.close()
            }
        }
    }

    companion object {
        fun create(context: Context, modelFileName: String = "recommendation_model.tflite"): LiteRTRecommendationEngine {
            val byteBuffer = loadModelBuffer(context, modelFileName)
            val options = Interpreter.Options()
            val interpreter = Interpreter(byteBuffer, options)
            return LiteRTRecommendationEngine(interpreter)
        }

        fun createFromBuffer(byteBuffer: ByteBuffer): LiteRTRecommendationEngine {
            val options = Interpreter.Options()
            val interpreter = Interpreter(byteBuffer, options)
            return LiteRTRecommendationEngine(interpreter)
        }

        private fun loadModelBuffer(context: Context, modelFileName: String): ByteBuffer {
            return try {
                val fileDescriptor = context.assets.openFd(modelFileName)
                val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            } catch (e: Exception) {
                val bytes = context.assets.open(modelFileName).readBytes()
                val buffer = ByteBuffer.allocateDirect(bytes.size)
                buffer.order(ByteOrder.nativeOrder())
                buffer.put(bytes)
                buffer.rewind()
                buffer
            }
        }
    }
}
