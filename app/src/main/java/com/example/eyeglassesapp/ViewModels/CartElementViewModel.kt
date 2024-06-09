package com.example.eyeglassesapp.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.entities.CartElementEntity
import com.example.eyeglassesapp.repositories.CartElementRepository
import kotlinx.coroutines.launch

class CartElementViewModel(private val cartElementRepository: CartElementRepository) : ViewModel() {

    fun getCartElementsByUserId(userId: Int): LiveData<List<CartElementEntity>> {
        return cartElementRepository.getCartElementsByUserId(userId)
    }
    fun deleteCartElement(cartElement: CartElementEntity) {
        viewModelScope.launch {
            cartElementRepository.deleteCartElement(cartElement)
        }
    }

    fun updateCartElement(cartElement: CartElementEntity) {
        viewModelScope.launch {
            cartElementRepository.updateCartElement(cartElement)
        }
    }
    fun deleteAllCartElements() {
        viewModelScope.launch {
            cartElementRepository.deleteAllCartElements()
        }
    }

    private val _totalCartItemCount = MutableLiveData<Int>()
    val totalCartItemCount: LiveData<Int> get() = _totalCartItemCount

    fun fetchTotalCartItemCount(userId: Int) {
        viewModelScope.launch {
            try {
                val count = cartElementRepository.getTotalCartItemCount(userId)
                _totalCartItemCount.value = count
                Log.e("CartViewModel", "Failed to fetch cart item count ${count} items")
            } catch (e: Exception) {
                _totalCartItemCount.value = 0
                Log.e("CartViewModel", "Failed to fetch cart item count", e)
            }
        }
    }
}