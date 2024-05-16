package com.example.eyeglassesapp.repositories

import androidx.lifecycle.LiveData
import com.example.eyeglassesapp.FrameWithImages
import com.example.eyeglassesapp.dao.FrameDao
import com.example.eyeglassesapp.entities.FrameEntity

class FrameRepository(private val frameDao: FrameDao) {
    suspend fun insertFrame(frame: FrameEntity) : Long{
        return frameDao.insertFrame(frame)
    }
    suspend fun updateFrame(frame: FrameEntity){
        frameDao.updateFrame(frame)
    }
    suspend fun deleteFrame(frame: FrameEntity){
        frameDao.deleteFrame(frame)
    }
    suspend fun getFrameById(frameId: Int): FrameEntity{
        return frameDao.getFrameById(frameId)
    }
    suspend fun getAllFrames(): List<FrameEntity>{
        return frameDao.getAllFrames()
    }
    fun getAllFramesWithImages(): LiveData<List<FrameWithImages>>{
        return frameDao.getAllFramesWithImages()
    }
    fun getAllFramesWithImagesLessThanPrice(price: Double): List<FrameWithImages>{
        return frameDao.getAllFramesWithImagesLessThanPrice(price)
    }
    fun getFramesByCategory(category: String) : List<FrameWithImages>{
        return frameDao.getFramesByCategory(category)
    }
    fun getFramesByGender(category: String, gender : String) : List<FrameWithImages>{
        return frameDao.getFramesByGender(category,gender)
    }
    suspend fun deleteFrameById(frameId: Int) {
        frameDao.deleteFrameById(frameId)
    }

    //frame with images for a specific frame id
    fun getFrameWithImagesById(frameId: Int): LiveData<FrameWithImages> {
        return frameDao.getFrameWithImagesById(frameId)
    }
    suspend fun getTotalFrameCount(): Int{
        return frameDao.getTotalFrameCount()
    }

    suspend fun getEyeglassesCount(): Int {
        return frameDao.getCountByCategory("Eyeglasses")
    }

    suspend fun getSunglassesCount(): Int {
        return frameDao.getCountByCategory("Sunglasses")
    }
}