package com.shopmind.recommendation.ml

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import kotlin.math.ln1p
import kotlin.math.max

@Serializable
data class FeatureMetadata(
    @SerialName("feature_dimension")
    val featureDimension: Int,
    @SerialName("feature_names")
    val featureNames: List<String>,
    @SerialName("model_input_dimension")
    val modelInputDimension: Int,
    @SerialName("model_input_contract")
    val modelInputContract: String,
    @SerialName("model_output")
    val modelOutput: String? = null,
    @SerialName("numeric_preprocessing")
    val numericPreprocessing: Map<String, NumericFeatureSpec>
) {
    val featureNameToIndex: Map<String, Int> = featureNames.mapIndexed { index, name -> name to index }.toMap()

    init {
        validate()
    }

    fun validate() {
        require(featureDimension == 229) {
            "Expected feature_dimension 229, got $featureDimension"
        }
        require(featureNames.size == featureDimension) {
            "feature_names size (${featureNames.size}) does not match feature_dimension ($featureDimension)"
        }
        require(modelInputDimension == 458) {
            "Expected model_input_dimension 458, got $modelInputDimension"
        }
        require(modelInputContract == "concatenate(user_profile, candidate_product_features)") {
            "Invalid model_input_contract: $modelInputContract"
        }
        require(numericPreprocessing.containsKey("price")) { "Missing price numeric preprocessing" }
        require(numericPreprocessing.containsKey("rating")) { "Missing rating numeric preprocessing" }
        require(numericPreprocessing.containsKey("discount")) { "Missing discount numeric preprocessing" }
    }

    fun transformPrice(price: Double): Float {
        val spec = numericPreprocessing["price"] ?: error("price spec missing")
        val transformed = ln1p(max(price, 0.0))
        return spec.minMax.normalize(transformed)
    }

    fun transformRating(rating: Double): Float {
        val spec = numericPreprocessing["rating"] ?: error("rating spec missing")
        return spec.minMax.normalize(rating)
    }

    fun transformDiscount(discount: Double): Float {
        val spec = numericPreprocessing["discount"] ?: error("discount spec missing")
        return spec.minMax.normalize(discount)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun loadFromAssets(context: Context, fileName: String = "feature_metadata.json"): FeatureMetadata {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            return parseJson(jsonString)
        }

        fun loadFromStream(inputStream: InputStream): FeatureMetadata {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            return parseJson(jsonString)
        }

        fun parseJson(jsonString: String): FeatureMetadata {
            return json.decodeFromString<FeatureMetadata>(jsonString)
        }
    }
}

@Serializable
data class NumericFeatureSpec(
    val transform: String,
    @SerialName("min_max")
    val minMax: MinMaxSpec
)

@Serializable
data class MinMaxSpec(
    val min: Double,
    val max: Double,
    @SerialName("constant_feature_uses_zero")
    val constantFeatureUsesZero: Boolean = false
) {
    fun normalize(value: Double): Float {
        if (max == min) {
            return if (constantFeatureUsesZero) 0f else 0.5f
        }
        val norm = (value - min) / (max - min)
        return norm.toFloat()
    }
}
