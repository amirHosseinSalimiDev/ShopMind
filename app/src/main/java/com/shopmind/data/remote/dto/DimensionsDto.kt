package com.shopmind.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DimensionsDto(
    val width: Double,
    val height: Double,
    val depth: Double
)
