package com.shopmind.recommendation.data

import com.shopmind.data.local.dao.ProductDao
import com.shopmind.data.local.datastore.UserDataStore
import com.shopmind.data.local.entity.ProductEntity
import com.shopmind.recommendation.ml.LiteRTRecommendationEngine
import com.shopmind.recommendation.ml.ProductFeatureEncoder
import com.shopmind.recommendation.ml.PurchasedItem
import com.shopmind.recommendation.ml.UserProfileBuilder
import kotlinx.coroutines.flow.firstOrNull

enum class RecommendationType {
    PERSONALIZED,
    FALLBACK
}

data class RecommendationResult(
    val products: List<ProductEntity>,
    val type: RecommendationType
)

interface RecommendationRepository {
    suspend fun getRecommendations(limit: Int = 10): RecommendationResult
    suspend fun getRecommendationsForUser(userId: Long, limit: Int = 10): RecommendationResult
}

open class RecommendationRepositoryImpl(
    private val productDao: ProductDao,
    private val userDataStore: UserDataStore?,
    private val encoder: ProductFeatureEncoder,
    private val profileBuilder: UserProfileBuilder,
    private val engine: LiteRTRecommendationEngine
) : RecommendationRepository {

    protected open suspend fun getCurrentUserId(): Long? {
        return userDataStore?.userId?.firstOrNull()
    }

    override suspend fun getRecommendations(limit: Int): RecommendationResult {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null || currentUserId <= 0L) {
            return getFallbackRecommendations(limit)
        }
        return getRecommendationsForUser(currentUserId, limit)
    }

    override suspend fun getRecommendationsForUser(userId: Long, limit: Int): RecommendationResult {
        // 1. Fetch completed purchase history
        val purchaseHistory = productDao.getCompletedPurchaseHistoryList(userId)
        if (purchaseHistory.isEmpty()) {
            return getFallbackRecommendations(limit)
        }

        // 2. Resolve purchased products
        val purchasedItems = purchaseHistory.map { historyItem ->
            val product = productDao.getProductById(historyItem.productId)
            PurchasedItem(product = product, quantity = historyItem.quantity)
        }

        // 3. Build user profile
        val userProfile = profileBuilder.buildProfile(purchasedItems)
        if (userProfile == null) {
            return getFallbackRecommendations(limit)
        }

        // 4. Fetch candidate products (stock > 0 and NOT in completed purchases)
        val candidateProducts = productDao.getCandidateProductsForUser(userId)
        if (candidateProducts.isEmpty()) {
            return getFallbackRecommendations(limit)
        }

        // 5. Encode candidate products
        val candidateVectors = candidateProducts.map { encoder.encode(it) }

        // 6. Run LiteRT inference
        val scores = engine.predictBatch(userProfile, candidateVectors)

        // 7. Sort candidates descending by score
        val scoredCandidates = candidateProducts.zip(scores.toList())
            .sortedByDescending { it.second }

        val topK = scoredCandidates.take(limit).map { it.first }

        return RecommendationResult(
            products = topK,
            type = RecommendationType.PERSONALIZED
        )
    }

    private suspend fun getFallbackRecommendations(limit: Int): RecommendationResult {
        val allProducts = productDao.getAllProductsList()
        val availableProducts = allProducts.filter { it.stock > 0 }
        val fallbackProducts = availableProducts
            .sortedByDescending { it.rating }
            .take(limit)

        return RecommendationResult(
            products = fallbackProducts,
            type = RecommendationType.FALLBACK
        )
    }
}
