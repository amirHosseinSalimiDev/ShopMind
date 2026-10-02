package com.shopmind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shopmind.data.local.entity.ProductEntity
import com.shopmind.data.local.entity.PurchaseHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsList(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("""
        SELECT 
            oi.productId,
            oi.quantity,
            oi.unitPrice,
            o.createdAt
        FROM order_items oi
        INNER JOIN orders o ON oi.orderId = o.id
        WHERE o.userId = :userId AND UPPER(o.status) = 'COMPLETED'
        ORDER BY o.createdAt DESC
    """)
    fun getPurchaseHistory(userId: Long): Flow<List<PurchaseHistoryItem>>

    @Query("""
        SELECT 
            oi.productId,
            oi.quantity,
            oi.unitPrice,
            o.createdAt
        FROM order_items oi
        INNER JOIN orders o ON oi.orderId = o.id
        WHERE o.userId = :userId AND UPPER(o.status) = 'COMPLETED'
        ORDER BY o.createdAt DESC
    """)
    suspend fun getCompletedPurchaseHistoryList(userId: Long): List<PurchaseHistoryItem>

    @Query("""
        SELECT * FROM products 
        WHERE id IN (
            SELECT oi.productId FROM order_items oi
            INNER JOIN orders o ON oi.orderId = o.id
            WHERE o.userId = :userId AND UPPER(o.status) = 'COMPLETED'
        )
    """)
    fun getPurchasedProducts(userId: Long): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE stock > 0 
          AND id NOT IN (
              SELECT oi.productId FROM order_items oi
              INNER JOIN orders o ON oi.orderId = o.id
              WHERE o.userId = :userId AND UPPER(o.status) = 'COMPLETED'
          )
    """)
    suspend fun getCandidateProductsForUser(userId: Long): List<ProductEntity>
}
