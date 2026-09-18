package com.shopmind.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetaDto(
    val createdAt: String,
    val updatedAt: String,
    val barcode: String,
    val qrCode: String
)
