package com.example.eyeglassesapp.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.entities.OrderEntity
import com.example.eyeglassesapp.repositories.OrderRepository
import kotlinx.coroutines.launch
import java.util.Date

class OrderViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    fun createOrder(userId: Int, totalNumberProducts: Int, totalPrice: Double): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch {
            try {
                val orderId = orderRepository.insertOrder(
                    OrderEntity(
                        userId = userId,
                        totalNumberProducts = totalNumberProducts,
                        orderDate = Date(),
                        totalPrice = totalPrice
                    )
                )
                result.postValue(orderId)
            } catch (e: Exception) {
                // Handle error
                Log.e("OrderViewModel", "Error creating order", e)
            }
        }
        return result
    }

    fun getOrderById(orderId: Long): LiveData<OrderEntity?> {
        val orderResultLiveData = MutableLiveData<OrderEntity?>()

        viewModelScope.launch {
            try {
                val order = orderRepository.getOrderById(orderId)
                orderResultLiveData.value = order
            } catch (e: Exception) {
                // Handle any errors here, e.g., log or notify UI
                orderResultLiveData.value = null
            }
        }

        return orderResultLiveData
    }

    //get orders by user id
    private val _ordersLiveData = MutableLiveData<List<OrderEntity>>()
    val ordersLiveData: LiveData<List<OrderEntity>>
        get() = _ordersLiveData

    fun getOrdersByUserId(userId: Int) {
        viewModelScope.launch {
            try {
                val orders = orderRepository.getOrdersByUserId(userId)
                _ordersLiveData.value = orders
            } catch (e: Exception) {
                Log.d("OrderDebug", "order list failed to load")
            }
        }
    }

    //delete order
    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            orderRepository.deleteOrder(order)
        }
    }


    //get all orders
    private val _allOrders = MutableLiveData<List<OrderEntity>>()
    val allOrders: LiveData<List<OrderEntity>>
        get() = _allOrders

    // Function to retrieve all orders
    fun getAllOrders() {
        viewModelScope.launch {
            _allOrders.value = orderRepository.getAllOrders()
        }
    }

    //get order count
    private val _totalOrderCount = MutableLiveData<Int>()
    val totalOrderCount : LiveData<Int> = _totalOrderCount

    fun getTotalOrderCount(){
        viewModelScope.launch {
            try {
                val count = orderRepository.getTotalOrderCount()
                _totalOrderCount.postValue(count)
            }catch (e:Exception){
                _totalOrderCount.postValue(0)
            }
        }
    }
}