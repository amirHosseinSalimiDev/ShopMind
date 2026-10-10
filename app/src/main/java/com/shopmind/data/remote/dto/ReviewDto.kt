package com.shopmind.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String,
    val reviewerEmail: String
)
