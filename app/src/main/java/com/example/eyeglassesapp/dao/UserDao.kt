package com.example.eyeglassesapp.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    // Query to retrieve all users from users table
    @Query("SELECT * FROM users")
    fun getAllUsers() : LiveData<List<UserEntity>>

    @Transaction
    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteUserById(userId: Int)

    @Query("SELECT user_id FROM users WHERE email = :userEmail LIMIT 1")
    suspend fun getUserIdByEmail(userEmail: String): Int?

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserByUserId(userId: Int): UserEntity?

    @Query("UPDATE users SET profile_picture_url = :imagePath WHERE user_id = :userId")
    suspend fun updateUserProfilePicture(userId: Int, imagePath: String)

    //admin side
    //count of all users
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUserCount() : Int

    @Query("SELECT COUNT(*) FROM users WHERE gender = 'male'")
    suspend fun getMaleUserCount(): Int

    @Query("SELECT COUNT(*) FROM users WHERE gender = 'female'")
    suspend fun getFemaleUserCount(): Int
}
