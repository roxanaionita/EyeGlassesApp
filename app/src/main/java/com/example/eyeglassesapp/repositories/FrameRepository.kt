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
    suspend fun deleteFrameById(frameId: Int) {
        frameDao.deleteFrameById(frameId)
    }

}