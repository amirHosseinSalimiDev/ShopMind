package com.shopmind.data.repository

import com.shopmind.data.local.dao.OrderDao
import com.shopmind.data.local.entity.OrderEntity
import com.shopmind.data.local.entity.OrderItemEntity

class OrderRepository(
    private val orderDao: OrderDao
) {
    suspend fun createOrder(
        userId: Long,
        totalAmount: Double,
        items: List<Pair<Int, Pair<Int, Double>>> // productId -> (quantity, unitPrice)
    ): Result<Long> {
        return try {
            val order = OrderEntity(
                userId = userId,
                totalAmount = totalAmount,
                status = "COMPLETED"
            )
            val orderId = orderDao.insertOrder(order)
            
            val orderItems = items.map { (productId, pair) ->
                val (quantity, unitPrice) = pair
                OrderItemEntity(
                    orderId = orderId,
                    productId = productId,
                    quantity = quantity,
                    unitPrice = unitPrice
                )
            }
            orderDao.insertOrderItems(orderItems)
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
