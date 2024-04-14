package com.example.eyeglassesapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eyeglassesapp.entities.OrderEntity

@Dao
interface OrderDao {
    // Insert a new order
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    // Delete an order by its entity
    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    // Retrieve all orders for a specific user
    @Query("SELECT * FROM orders WHERE user_id = :userId")
    suspend fun getOrdersByUserId(userId: Int): List<OrderEntity>

    // Optional: Retrieve all orders
    @Query("SELECT * FROM orders")
    suspend fun getAllOrders(): List<OrderEntity>

//
}