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
    WHERE o.userId = :userId
    ORDER BY o.createdAt DESC
""")
    fun getPurchaseHistory(userId: Long): Flow<List<PurchaseHistoryItem>>

    @Query("""
        SELECT * FROM products 
        WHERE id IN (
            SELECT productId FROM order_items 
            WHERE orderId IN (
                SELECT id FROM orders WHERE userId = :userId
            )
        )
    """)
    fun getPurchasedProducts(userId: Long): Flow<List<ProductEntity>>
}
