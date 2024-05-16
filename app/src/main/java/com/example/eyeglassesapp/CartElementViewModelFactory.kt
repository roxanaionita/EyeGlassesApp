package com.example.eyeglassesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eyeglassesapp.ViewModels.CartElementViewModel
import com.example.eyeglassesapp.repositories.CartElementRepository

class CartElementViewModelFactory(private val cartElementRepository: CartElementRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartElementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartElementViewModel(cartElementRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}