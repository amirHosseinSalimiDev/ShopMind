package com.shopmind.recommendation.ml

import com.shopmind.data.local.entity.ProductEntity

class ProductFeatureEncoder(
    private val metadata: FeatureMetadata
) {
    fun encode(product: ProductEntity): FloatArray {
        val vector = FloatArray(metadata.featureDimension) { 0.0f }

        // 1. One-hot encode Category
        val cleanCategory = product.category.trim().lowercase()
        val categoryFeatureName = "category_$cleanCategory"
        metadata.featureNameToIndex[categoryFeatureName]?.let { index ->
            vector[index] = 1.0f
        }

        // 2. One-hot encode Brand
        val rawBrand = product.brand.trim()
        val cleanBrand = if (rawBrand.isBlank() || rawBrand.lowercase() == "unknown") {
            "UNKNOWN"
        } else {
            rawBrand.lowercase()
        }
        val brandFeatureName = "brand_$cleanBrand"
        val brandIndex = metadata.featureNameToIndex[brandFeatureName]
            ?: metadata.featureNameToIndex["brand_UNKNOWN"]
        brandIndex?.let { index ->
            vector[index] = 1.0f
        }

        // 3. Multi-hot encode Tags
        val parsedTags = parseTags(product.tags)
        for (tag in parsedTags) {
            val tagFeatureName = "tag_$tag"
            metadata.featureNameToIndex[tagFeatureName]?.let { index ->
                vector[index] = 1.0f
            }
        }

        // 4. Numeric Features
        metadata.featureNameToIndex["price"]?.let { index ->
            vector[index] = metadata.transformPrice(product.price)
        }
        metadata.featureNameToIndex["rating"]?.let { index ->
            vector[index] = metadata.transformRating(product.rating)
        }
        metadata.featureNameToIndex["discount"]?.let { index ->
            vector[index] = metadata.transformDiscount(product.discountPercentage)
        }

        return vector
    }

    private fun parseTags(tags: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (rawTag in tags) {
            val trimmed = rawTag.trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                val stripped = trimmed.removePrefix("[").removeSuffix("]").replace("\"", "").replace("'", "")
                stripped.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.forEach { result.add(it) }
            } else if (trimmed.isNotEmpty()) {
                result.add(trimmed.lowercase())
            }
        }
        return result
    }
}
