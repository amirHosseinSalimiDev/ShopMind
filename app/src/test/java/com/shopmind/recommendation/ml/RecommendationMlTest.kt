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
}
