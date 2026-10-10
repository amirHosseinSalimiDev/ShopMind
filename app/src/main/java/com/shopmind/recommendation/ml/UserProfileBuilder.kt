package com.shopmind.recommendation.ml

import com.shopmind.data.local.entity.ProductEntity
import kotlin.math.ln1p
import kotlin.math.max
import kotlin.math.sqrt

data class PurchasedItem(
    val product: ProductEntity?,
    val quantity: Int
)

class UserProfileBuilder(
    private val encoder: ProductFeatureEncoder
) {
    /**
     * Builds a 229-element L2-normalized user profile vector from purchased items.
     * If a product in [items] is null or cannot be found/encoded, it is ignored according to policy.
     * Returns null if no valid products exist or if the resulting profile is a zero vector.
     */
    fun buildProfile(items: List<PurchasedItem>): FloatArray? {
        val validItems = items.filter { it.product != null }
        if (validItems.isEmpty()) return null

        val featureDim = 229
        val weightedSum = DoubleArray(featureDim)
        var totalWeight = 0.0

        for (item in validItems) {
            val product = item.product ?: continue
            val vector = encoder.encode(product)
            val qty = max(item.quantity.toDouble(), 0.0)
            val weight = ln1p(qty)

            for (i in 0 until featureDim) {
                weightedSum[i] += vector[i] * weight
            }
            totalWeight += weight
        }

        if (totalWeight <= 0.0) return null

        val meanProfile = FloatArray(featureDim) { i ->
            (weightedSum[i] / totalWeight).toFloat()
        }

        // Calculate L2 norm
        var sumSquares = 0.0
        for (v in meanProfile) {
            sumSquares += v * v
        }
        val norm = sqrt(sumSquares)

        if (norm <= 0.0) return null

        // Apply L2 normalization
        val normalizedProfile = FloatArray(featureDim) { i ->
            (meanProfile[i] / norm).toFloat()
        }

        return normalizedProfile
    }
}
