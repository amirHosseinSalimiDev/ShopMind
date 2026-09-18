package com.shopmind.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shopmind.data.local.dao.OrderDao
import com.shopmind.data.local.dao.ProductDao
import com.shopmind.data.local.dao.UserDao
import com.shopmind.data.local.entity.OrderEntity
import com.shopmind.data.local.entity.OrderItemEntity
import com.shopmind.data.local.entity.ProductEntity
import com.shopmind.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
}
