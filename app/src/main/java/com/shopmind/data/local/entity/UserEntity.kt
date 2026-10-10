package com.shopmind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val phoneNumber: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)
