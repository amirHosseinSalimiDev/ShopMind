package com.shopmind.data.repository

import com.shopmind.data.local.dao.UserDao
import com.shopmind.data.local.datastore.UserDataStore
import com.shopmind.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val userDataStore: UserDataStore,
    private val userDao: UserDao
) {

    val isLoggedIn: Flow<Boolean> = userDataStore.isLoggedIn
    val userId: Flow<Long?> = userDataStore.userId

    suspend fun signup(user: UserEntity): Result<Unit> {
        return try {
            val existingUser = userDao.getUserByPhoneNumber(user.phoneNumber)
            if (existingUser != null) {
                Result.failure(Exception("User already exists"))
            } else {
                val userId = userDao.insertUser(user)
                userDataStore.saveSession(userId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(phoneNumber: String, password: String): Result<Unit> {
        return try {
            val user = userDao.getUserByPhoneNumber(phoneNumber)
            if (user != null && user.password == password) {
                userDataStore.saveSession(user.id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid phone number or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userDataStore.clearSession()
    }
}
