package com.example.eyeglassesapp.repositories

import androidx.lifecycle.LiveData
import com.example.eyeglassesapp.FrameWithImages
import com.example.eyeglassesapp.dao.FrameFaceShape_CrossDao
import com.example.eyeglassesapp.entities.FaceShapeEntity
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FrameFaceShape_CrossRepository (private val FrameFaceShape_CrossDao : FrameFaceShape_CrossDao) {
    suspend fun insert(crossEntity: FrameFaceShape_CrossEntity) {
        withContext(Dispatchers.IO) {
            FrameFaceShape_CrossDao.insert(crossEntity)
        }
    }

    suspend fun insertAll(crossEntities: List<FrameFaceShape_CrossEntity>) {
        withContext(Dispatchers.IO) {
            FrameFaceShape_CrossDao.insertAll(crossEntities)
        }
    }

    suspend fun getFaceShapesForFrame(frameId: Int): List<FrameFaceShape_CrossEntity> {
        return withContext(Dispatchers.IO) {
            FrameFaceShape_CrossDao.getFaceShapesForFrame(frameId)
        }
    }

    suspend fun getFramesForFaceShape(faceShapeId: Int): List<FrameFaceShape_CrossEntity> {
        return withContext(Dispatchers.IO) {
            FrameFaceShape_CrossDao.getFramesForFaceShape(faceShapeId)
        }
    }

    suspend fun getFramesWithImagesByFaceShapeIdAndCategory(faceShapeId: Int, category: String): List<FrameWithImages> {
        return FrameFaceShape_CrossDao.getFramesWithImagesByFaceShapeIdAndCategory(faceShapeId, category)
    }

    fun getFramesForFaceShape(faceShape: String): LiveData<List<FrameWithImages>> {
        return FrameFaceShape_CrossDao.getFramesForFaceShape(faceShape)
    }
}