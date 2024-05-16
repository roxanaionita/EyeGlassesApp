package com.example.eyeglassesapp.repositories

import androidx.lifecycle.LiveData
import com.example.eyeglassesapp.dao.LensDao
import com.example.eyeglassesapp.entities.LensEntity

class LensRepository (private val lensDao: LensDao) {
    suspend fun insertLens(lens: LensEntity): Long{
        return lensDao.insertLens(lens)
    }
    suspend fun deleteLens(lens: LensEntity){
        lensDao.deleteLens(lens)
    }
    suspend fun getAllLenses(): List<LensEntity>{
        return lensDao.getAllLenses()
    }
    suspend fun getLensById(lensId: Int): LensEntity?{
        return lensDao.getLensById(lensId)
    }

    fun getPriceForLens(lensType: String, material: String, isUVFilterSelected: Boolean, isPCFilterSelected: Boolean): LiveData<Double?> {
        return lensDao.getPriceForLens(lensType, material, isUVFilterSelected, isPCFilterSelected)
    }
    fun getLensId(lensType: String, material: String, uvFilter: Boolean, pcFilter: Boolean): LiveData<Int?> {
        return lensDao.getLensId(lensType, material, uvFilter, pcFilter)
    }

}