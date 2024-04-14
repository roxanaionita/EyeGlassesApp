package com.example.eyeglassesapp.repositories

import com.example.eyeglassesapp.dao.ImageDao
import com.example.eyeglassesapp.entities.ImageEntity

class ImageRepository (private val imageDao: ImageDao) {

    suspend fun insertImage(image:ImageEntity){
        imageDao.insertImage(image)
    }
    suspend fun updateImage(image: ImageEntity) {
        imageDao.updateImage(image)
    }
    suspend fun findImagesByFrameId(frameId: Int) : List<ImageEntity>{
        return imageDao.findImagesByFrameId(frameId)
    }
    suspend fun deleteImageById(imageId: Int){
        imageDao.deleteImageById(imageId)
    }
    suspend fun deleteImagesByFrameId(frameId: Int){
        imageDao.deleteImagesByFrameId(frameId)
    }
}