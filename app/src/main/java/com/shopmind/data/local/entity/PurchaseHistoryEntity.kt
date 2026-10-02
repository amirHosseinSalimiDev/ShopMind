package com.shopmind.data.local.entity

data class PurchaseHistoryItem(
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double,
    val createdAt: Long
)