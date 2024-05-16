package com.example.eyeglassesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eyeglassesapp.ViewModels.PairViewModel
import com.example.eyeglassesapp.repositories.CartElementRepository
import com.example.eyeglassesapp.repositories.PairRepository

class PairViewModelFactory(private val pairRepository: PairRepository,
                           private val cartElementRepository: CartElementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PairViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PairViewModel(pairRepository, cartElementRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}