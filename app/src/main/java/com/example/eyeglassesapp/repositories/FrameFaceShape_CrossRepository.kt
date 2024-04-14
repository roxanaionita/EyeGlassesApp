package com.example.eyeglassesapp.repositories

import com.example.eyeglassesapp.dao.FrameFaceShape_CrossDao
import com.example.eyeglassesapp.entities.FaceShapeEntity
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity

class FrameFaceShape_CrossRepository (private val FrameFaceShape_CrossDao : FrameFaceShape_CrossDao) {
    suspend fun insertFrameFaceShapeCrossRef(crossRef: FrameFaceShape_CrossEntity) {
        FrameFaceShape_CrossDao.insertFrameFaceShapeCrossRef(crossRef)
    }
    suspend fun getFramesForFaceShape(faceShapeId: Int): List<FrameEntity> {
        return FrameFaceShape_CrossDao.getFramesForFaceShape(faceShapeId)
    }
    suspend fun getFaceShapesForFrame(frameId: Int): List<FaceShapeEntity>{
        return FrameFaceShape_CrossDao.getFaceShapesForFrame(frameId)
    }
    suspend fun deleteAssociation(frameId: Int, faceShapeId: Int){
        FrameFaceShape_CrossDao.deleteAssociation(frameId, faceShapeId)
    }
}