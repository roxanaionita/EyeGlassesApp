package com.example.eyeglassesapp.repositories

import com.example.eyeglassesapp.dao.UserDao
import com.example.eyeglassesapp.entities.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.getUserByUid(userId)
    }

}