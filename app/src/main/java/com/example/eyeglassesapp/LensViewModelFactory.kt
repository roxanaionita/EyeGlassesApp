package com.example.eyeglassesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eyeglassesapp.ViewModels.LensViewModel
import com.example.eyeglassesapp.repositories.LensRepository


class LensViewModelFactory(private val lensRepository: LensRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LensViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LensViewModel(lensRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}