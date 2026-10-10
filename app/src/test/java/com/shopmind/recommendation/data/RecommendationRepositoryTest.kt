package com.shopmind.recommendation.data

import com.shopmind.data.local.dao.ProductDao
import com.shopmind.data.local.entity.ProductEntity
import com.shopmind.data.local.entity.PurchaseHistoryItem
import com.shopmind.recommendation.ml.FeatureMetadata
import com.shopmind.recommendation.ml.LiteRTRecommendationEngine
import com.shopmind.recommendation.ml.ProductFeatureEncoder
import com.shopmind.recommendation.ml.UserProfileBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class RecommendationRepositoryTest {

    private lateinit var metadata: FeatureMetadata
    private lateinit var encoder: ProductFeatureEncoder
    private lateinit var profileBuilder: UserProfileBuilder

    private val sampleProducts = listOf(
        ProductEntity(1, "beauty", "apple", listOf("beauty"), 10.0, 0.0, 4.5, "P1", "", emptyList(), "", 10, "In Stock"),
        ProductEntity(2, "beauty", "apple", listOf("beauty"), 20.0, 0.0, 4.8, "P2", "", emptyList(), "", 5, "In Stock"),
        ProductEntity(3, "furniture", "ikea", listOf("home"), 30.0, 0.0, 3.9, "P3", "", emptyList(), "", 0, "Out of Stock"), // Stock 0
        ProductEntity(4, "groceries", "nestle", listOf("food"), 5.0, 0.0, 4.9, "P4", "", emptyList(), "", 12, "In Stock")
    )

    @Before
    fun setUp() {
        val assetFile = File("src/main/assets/feature_metadata.json").let {
            if (it.exists()) it else File("app/src/main/assets/feature_metadata.json")
        }
        metadata = FeatureMetadata.loadFromStream(assetFile.inputStream())
        encoder = ProductFeatureEncoder(metadata)
        profileBuilder = UserProfileBuilder(encoder)
    }

    @Test
    fun testUserWithNoPurchasesReturnsFallback() = runBlocking {
        val fakeProductDao = FakeProductDao(
            products = sampleProducts,
            purchases = emptyList()
        )

        val testEngine = LiteRTRecommendationEngine(
            batchPredictor = { _, candidates -> FloatArray(candidates.size) { 0.5f } }
        )

        val repo = RecommendationRepositoryImpl(
            productDao = fakeProductDao,
            userDataStore = null,
            encoder = encoder,
            profileBuilder = profileBuilder,
            engine = testEngine
        )

        val result = repo.getRecommendationsForUser(userId = 9999L, limit = 2)

        assertEquals(RecommendationType.FALLBACK, result.type)
        assertEquals(2, result.products.size)
        // Fallback sorted by rating descending (P4 rating 4.9, P2 rating 4.8)
        assertEquals(4, result.products[0].id)
        assertEquals(2, result.products[1].id)
    }

    @Test
    fun testUserWithPurchasesReturnsPersonalized() = runBlocking {
        val fakeProductDao = FakeProductDao(
            products = sampleProducts,
            purchases = listOf(PurchaseHistoryItem(productId = 1, quantity = 2, unitPrice = 10.0, createdAt = 1000L))
        )

        // Test engine assigns higher relevance score to candidate P2 over candidate P4
        val testEngine = LiteRTRecommendationEngine(
            batchPredictor = { _, candidates ->
                FloatArray(candidates.size) { index -> if (index == 0) 0.85f else 0.60f }
            }
        )

        val repo = RecommendationRepositoryImpl(
            productDao = fakeProductDao,
            userDataStore = null,
            encoder = encoder,
            profileBuilder = profileBuilder,
            engine = testEngine
        )

        val result = repo.getRecommendationsForUser(userId = 10026L, limit = 5)

        assertEquals(RecommendationType.PERSONALIZED, result.type)
        // Candidates are product 2 and product 4 (product 1 was purchased, product 3 is out of stock)
        assertEquals(2, result.products.size)
        assertEquals(2, result.products[0].id)
        assertEquals(4, result.products[1].id)
    }

    @Test
    fun testCandidateFilteringExcludesPurchasedAndOutOfStock() = runBlocking {
        val fakeProductDao = FakeProductDao(
            products = sampleProducts,
            purchases = listOf(PurchaseHistoryItem(productId = 1, quantity = 1, unitPrice = 10.0, createdAt = 1000L))
        )

        val candidates = fakeProductDao.getCandidateProductsForUser(userId = 10026L)

        // Product 1 is purchased -> excluded
        // Product 3 has stock = 0 -> excluded
        // Candidates should be Products 2 and 4
        assertEquals(2, candidates.size)
        assertEquals(listOf(2, 4), candidates.map { it.id })
    }
}

private class FakeProductDao(
    private val products: List<ProductEntity>,
    private val purchases: List<PurchaseHistoryItem>
) : ProductDao {
    override suspend fun insertProducts(products: List<ProductEntity>) {}
    override fun getAllProducts(): Flow<List<ProductEntity>> = flowOf(products)
    override suspend fun getAllProductsList(): List<ProductEntity> = products
    override suspend fun getProductById(productId: Int): ProductEntity? = products.find { it.id == productId }
    override fun getProductsByCategory(category: String): Flow<List<ProductEntity>> = flowOf(products.filter { it.category == category })
    override fun getPurchaseHistory(userId: Long): Flow<List<PurchaseHistoryItem>> = flowOf(purchases)
    override suspend fun getCompletedPurchaseHistoryList(userId: Long): List<PurchaseHistoryItem> = purchases
    override fun getPurchasedProducts(userId: Long): Flow<List<ProductEntity>> = flowOf(products.filter { p -> purchases.any { it.productId == p.id } })
    override suspend fun getCandidateProductsForUser(userId: Long): List<ProductEntity> {
        val purchasedIds = purchases.map { it.productId }.toSet()
        return products.filter { it.stock > 0 && it.id !in purchasedIds }
    }
}
