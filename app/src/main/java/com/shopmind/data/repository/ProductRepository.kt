package com.shopmind.data.repository

import com.shopmind.data.remote.api.ProductApiService
import com.shopmind.data.remote.dto.CategoryDto
import com.shopmind.data.remote.dto.ProductListResponseDto

class ProductRepository(
    private val productApiService: ProductApiService
) {
    suspend fun getAllProducts(): Result<ProductListResponseDto> {
        return try {
            Result.success(productApiService.getAllProducts())
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
            Result.success(productApiService.getProductsByCategory(category))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
