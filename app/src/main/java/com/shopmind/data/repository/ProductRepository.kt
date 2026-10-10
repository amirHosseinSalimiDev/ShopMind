package com.shopmind.data.repository

import com.shopmind.data.local.dao.ProductDao
import com.shopmind.data.local.entity.ProductEntity
import com.shopmind.data.remote.api.ProductApiService
import com.shopmind.data.remote.dto.CategoryDto
import com.shopmind.data.remote.dto.ProductDto
import com.shopmind.data.remote.dto.ProductListResponseDto

class ProductRepository(
    private val productApiService: ProductApiService,
    private val productDao: ProductDao
) {
    suspend fun getAllProducts(): Result<ProductListResponseDto> {
        return try {
            val response = productApiService.getAllProducts()
            // Cache in Room database (Phase 3)
            val entities = response.products.map { it.toEntity() }
            productDao.insertProducts(entities)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<CategoryDto>> {
        return try {
            Result.success(productApiService.getCategories())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(category: String): Result<ProductListResponseDto> {
        return try {
            val response = productApiService.getProductsByCategory(category)
            // Cache in Room database (Phase 3)
            val entities = response.products.map { it.toEntity() }
            productDao.insertProducts(entities)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ProductDto.toEntity(): ProductEntity {
        return ProductEntity(
            id = id,
            category = category,
            brand = brand ?: "Unknown",
            tags = tags,
            price = price,
            discountPercentage = discountPercentage,
            rating = rating,
            title = title,
            description = description,
            images = images,
            thumbnail = thumbnail,
            stock = stock,
            availabilityStatus = availabilityStatus
        )
    }
}
