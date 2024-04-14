package com.example.eyeglassesapp.repositories

import com.example.eyeglassesapp.dao.FaceShapeDao
import com.example.eyeglassesapp.entities.FaceShapeEntity

class FaceShapeRepository (private val FaceShapeDao : FaceShapeDao) {
    suspend fun insertFaceShape(faceShape: FaceShapeEntity): Long{
        return FaceShapeDao.insertFaceShape(faceShape)
    }
    suspend fun getFaceShapeById(id: Int): FaceShapeEntity?{
        return FaceShapeDao.getFaceShapeById(id)
    }
    suspend fun getAllFaceShapes(): List<FaceShapeEntity>{
        return FaceShapeDao.getAllFaceShapes()
    }
}