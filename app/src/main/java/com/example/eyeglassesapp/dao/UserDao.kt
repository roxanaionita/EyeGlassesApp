package com.example.eyeglassesapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eyeglassesapp.entities.UserEntity


@Dao
interface UserDao {

    // Insert a new user or replace an existing user (based on primary key or unique constraints)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    // Delete a user from the database
    @Delete
    suspend fun deleteUser(user: UserEntity)

    // Update an existing user's information
    @Update
    suspend fun updateUser(user: UserEntity)

    // Query to find a user by their UID (from Firebase)
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserByUid(userId: Int): UserEntity?
}
