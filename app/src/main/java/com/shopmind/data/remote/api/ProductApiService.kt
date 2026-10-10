package com.shopmind.data.remote.api

import com.shopmind.data.remote.dto.CategoryDto
import com.shopmind.data.remote.dto.ProductDto
import com.shopmind.data.remote.dto.ProductListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {

    @GET("products")
    suspend fun getAllProducts(
        @Query("limit") limit: Int = 0,
        @Query("skip") skip: Int = 0
    ): ProductListResponseDto

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int
    ): ProductDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String
    ): ProductListResponseDto

    @GET("products/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String
    ): ProductListResponseDto
}
