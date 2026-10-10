package com.shopmind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: Int,
    val category: String,
    val brand: String,
    val tags: List<String>,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val title: String,
    val description: String,
    val images: List<String>,
    val thumbnail: String,
    val stock: Int,
    val availabilityStatus: String
)
