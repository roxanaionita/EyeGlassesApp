package com.example.eyeglassesapp.repositories

import com.example.eyeglassesapp.dao.OrderDao
import com.example.eyeglassesapp.entities.OrderEntity

class OrderRepository(private val orderDao: OrderDao) {

    // Method to insert an order
    suspend fun insertOrder(order: OrderEntity): Long {
        return orderDao.insertOrder(order)
    }

    // Method to delete an order
    suspend fun deleteOrder(order: OrderEntity) {
        orderDao.deleteOrder(order)
    }

    suspend fun getOrdersByUserId(userId: Int): List<OrderEntity> {
        return orderDao.getOrdersByUserId(userId)
    }

    suspend fun getAllOrders() : List<OrderEntity>{
        return orderDao.getAllOrders()
    }

    suspend fun getOrderById(orderId: Long): OrderEntity? {
        return orderDao.getOrderById(orderId)
    }
    suspend fun getTotalOrderCount(): Int{
        return orderDao.getTotalOrderCount()
    }
}