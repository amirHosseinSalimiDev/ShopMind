package com.shopmind.recommendation.ml

import com.shopmind.data.local.entity.ProductEntity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.math.sqrt

class RecommendationMlTest {

    private lateinit var metadata: FeatureMetadata
    private lateinit var encoder: ProductFeatureEncoder
    private lateinit var profileBuilder: UserProfileBuilder

    @Before
    fun setUp() {
        // Find asset file relative to project root or test path
        val assetFile = File("src/main/assets/feature_metadata.json").let {
            if (it.exists()) it else File("app/src/main/assets/feature_metadata.json")
        }
        assertTrue("feature_metadata.json must exist in assets", assetFile.exists())

        metadata = FeatureMetadata.loadFromStream(assetFile.inputStream())
        encoder = ProductFeatureEncoder(metadata)
        profileBuilder = UserProfileBuilder(encoder)
    }

    @Test
    fun testFeatureMetadataValidation() {
        assertEquals(229, metadata.featureDimension)
        assertEquals(229, metadata.featureNames.size)
        assertEquals(458, metadata.modelInputDimension)
        assertEquals("concatenate(user_profile, candidate_product_features)", metadata.modelInputContract)
        assertEquals(229, metadata.featureNameToIndex.size)
        assertTrue(metadata.featureNameToIndex.containsKey("category_beauty"))
        assertTrue(metadata.featureNameToIndex.containsKey("brand_apple"))
        assertTrue(metadata.featureNameToIndex.containsKey("brand_UNKNOWN"))
        assertTrue(metadata.featureNameToIndex.containsKey("price"))
        assertTrue(metadata.featureNameToIndex.containsKey("rating"))
        assertTrue(metadata.featureNameToIndex.containsKey("discount"))
    }

    @Test
    fun testProductFeatureEncoder() {
        val testProduct = ProductEntity(
            id = 1,
            category = "beauty",
            brand = "apple",
            tags = listOf("beauty", "skincare"),
            price = 9.99,
            discountPercentage = 10.0,
            rating = 4.5,
            title = "Test Product",
            description = "Test Desc",
            images = emptyList(),
            thumbnail = "",
            stock = 10,
            availabilityStatus = "In Stock"
        )

        val vector = encoder.encode(testProduct)
        assertEquals(229, vector.size)

        // Category check
        val catIndex = metadata.featureNameToIndex["category_beauty"]
        assertNotNull(catIndex)
        assertEquals(1.0f, vector[catIndex!!], 0.0001f)

        // Brand check
        val brandIndex = metadata.featureNameToIndex["brand_apple"]
        assertNotNull(brandIndex)
        assertEquals(1.0f, vector[brandIndex!!], 0.0001f)

        // Tag check
        val tagIndex = metadata.featureNameToIndex["tag_beauty"]
        assertNotNull(tagIndex)
        assertEquals(1.0f, vector[tagIndex!!], 0.0001f)

        // Price, Rating, Discount check
        val priceIndex = metadata.featureNameToIndex["price"]!!
        val ratingIndex = metadata.featureNameToIndex["rating"]!!
        val discountIndex = metadata.featureNameToIndex["discount"]!!

        assertTrue("Price feature should be between 0 and 1", vector[priceIndex] in 0f..1f)
        assertTrue("Rating feature should be between 0 and 1", vector[ratingIndex] in 0f..1f)
        assertTrue("Discount feature should be between 0 and 1", vector[discountIndex] in 0f..1f)
    }

    @Test
    fun testUnknownBrandFallback() {
        val testProduct = ProductEntity(
            id = 2,
            category = "beauty",
            brand = "NonExistentBrand123",
            tags = emptyList(),
            price = 15.0,
            discountPercentage = 0.0,
            rating = 3.0,
            title = "Test",
            description = "",
            images = emptyList(),
            thumbnail = "",
            stock = 5,
            availabilityStatus = "In Stock"
        )

        val vector = encoder.encode(testProduct)
        val unknownBrandIndex = metadata.featureNameToIndex["brand_UNKNOWN"]
        assertNotNull(unknownBrandIndex)
        assertEquals(1.0f, vector[unknownBrandIndex!!], 0.0001f)
    }

    @Test
    fun testUserProfileBuilder() {
        val product1 = ProductEntity(
            id = 1,
            category = "beauty",
            brand = "apple",
            tags = listOf("beauty"),
            price = 10.0,
            discountPercentage = 5.0,
            rating = 4.0,
            title = "P1",
            description = "",
            images = emptyList(),
            thumbnail = "",
            stock = 10,
            availabilityStatus = "In Stock"
        )

        val items = listOf(
            PurchasedItem(product1, quantity = 2),
            PurchasedItem(null, quantity = 1) // Should be ignored
        )

        val profile = profileBuilder.buildProfile(items)
        assertNotNull("Profile should not be null for valid purchases", profile)
        assertEquals(229, profile!!.size)

        // Verify L2 norm is 1.0
        var sumSquares = 0.0
        for (v in profile) {
            sumSquares += v * v
        }
        val norm = sqrt(sumSquares)
        assertEquals("L2 norm of profile must be 1.0", 1.0, norm, 0.001)
    }

    @Test
    fun testNumericPreprocessingExactFormula() {
        val testProduct = ProductEntity(
            id = 10,
            category = "beauty",
            brand = "apple",
            tags = emptyList(),
            price = 9.99,
            discountPercentage = 10.0,
            rating = 4.5,
            title = "Test Numeric",
            description = "",
            images = emptyList(),
            thumbnail = "",
            stock = 5,
            availabilityStatus = "In Stock"
        )

        val vector = encoder.encode(testProduct)
        val priceIndex = metadata.featureNameToIndex["price"]!!
        val ratingIndex = metadata.featureNameToIndex["rating"]!!
        val discountIndex = metadata.featureNameToIndex["discount"]!!

        // Price: log1p(9.99) = ln(10.99) = ~2.3970
        // (2.3970 - 0.5822156) / (10.5186996 - 0.5822156) = ~0.18263
        val expectedPriceNorm = (kotlin.math.ln(1.0 + 9.99).toFloat() - 0.5822156f) / (10.5186996f - 0.5822156f)
        assertEquals(expectedPriceNorm, vector[priceIndex], 0.001f)

        // Rating: (4.5 - 2.5099999) / (4.9899998 - 2.5099999) = ~0.80242
        val expectedRatingNorm = (4.5f - 2.5099999f) / (4.9899998f - 2.5099999f)
        assertEquals(expectedRatingNorm, vector[ratingIndex], 0.001f)

        // Discount: (10.0 - 0.04) / (19.61 - 0.04) = ~0.50894
        val expectedDiscountNorm = (10.0f - 0.04f) / (19.61f - 0.04f)
        assertEquals(expectedDiscountNorm, vector[discountIndex], 0.001f)
    }

    @Test
    fun testModelInputConcatenationAndOrder() {
        val userProfile = FloatArray(229) { index -> index.toFloat() }
        val candidateProduct = FloatArray(229) { index -> (1000 + index).toFloat() }

        val modelInput = FloatArray(458)
        System.arraycopy(userProfile, 0, modelInput, 0, 229)
        System.arraycopy(candidateProduct, 0, modelInput, 229, 229)

        assertEquals(458, modelInput.size)

        // Verify userProfile is in 0..228
        for (i in 0 until 229) {
            assertEquals(i.toFloat(), modelInput[i], 0.0001f)
        }

        // Verify candidateProduct is in 229..457
        for (i in 0 until 229) {
            assertEquals((1000 + i).toFloat(), modelInput[229 + i], 0.0001f)
        }
    }

    @Test
    fun testZeroVectorUserProfileHandling() {
        val emptyItems = emptyList<PurchasedItem>()
        assertNull("Empty purchase list must return null profile", profileBuilder.buildProfile(emptyItems))

        val zeroQuantityItems = listOf(PurchasedItem(ProductEntity(1, "beauty", "apple", emptyList(), 10.0, 0.0, 4.0, "P", "", emptyList(), "", 5, ""), quantity = 0))
        assertNull("Zero quantity purchase list must return null profile", profileBuilder.buildProfile(zeroQuantityItems))
    }
}
