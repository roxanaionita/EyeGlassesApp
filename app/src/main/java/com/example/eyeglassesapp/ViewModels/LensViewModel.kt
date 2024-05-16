package com.example.eyeglassesapp.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.eyeglassesapp.repositories.LensRepository

class LensViewModel(private val lensRepository: LensRepository) : ViewModel() {

    fun getPriceForLens(lensType: String, material: String, isUVFilterSelected: Boolean, isPCFilterSelected: Boolean): LiveData<Double?> {
        return lensRepository.getPriceForLens(lensType, material, isUVFilterSelected, isPCFilterSelected)
    }
    fun getLensId(lensType: String, material: String, uvFilter: Boolean, pcFilter: Boolean): LiveData<Int?> {
        return lensRepository.getLensId(lensType, material, uvFilter, pcFilter)
    }
}