package com.example.eyeglassesapp.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.AppDatabase
import com.example.eyeglassesapp.FrameWithImages
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity
import com.example.eyeglassesapp.repositories.FrameFaceShape_CrossRepository
import com.example.eyeglassesapp.repositories.FrameRepository
import kotlinx.coroutines.launch

class FrameFaceShapeCrossViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FrameFaceShape_CrossRepository

    init {
        val crossDao = AppDatabase.getDatabase(application).frameFaceShape_CrossDao()
        repository = FrameFaceShape_CrossRepository(crossDao)
    }

    fun insert(crossEntity: FrameFaceShape_CrossEntity) {
        viewModelScope.launch {
            repository.insert(crossEntity)
        }
    }

    fun insertAll(crossEntities: List<FrameFaceShape_CrossEntity>) {
        viewModelScope.launch {
            repository.insertAll(crossEntities)
        }
    }

    fun getFaceShapesForFrame(frameId: Int) = viewModelScope.launch {
        repository.getFaceShapesForFrame(frameId)
    }

    fun getFramesForFaceShape(faceShapeId: Int) = viewModelScope.launch {
        repository.getFramesForFaceShape(faceShapeId)
    }

//    val framesWithImagesLiveData = MutableLiveData<List<FrameWithImages>>()
//    fun getFramesWithImagesByFaceShapeIdAndCategory(faceShapeId: Int, category: String) {
//        viewModelScope.launch {
//            val framesWithImages = repository.getFramesWithImagesByFaceShapeIdAndCategory(faceShapeId, category)
//            framesWithImagesLiveData.postValue(framesWithImages)
//        }
//    }


    //face shape using frame repo
    private val frameRepository: FrameRepository = FrameRepository(AppDatabase.getDatabase(application).frameDao())

    private val _framesWithImagesLiveData = MutableLiveData<List<FrameWithImages>>()
    val framesWithImagesLiveData: LiveData<List<FrameWithImages>>
        get() = _framesWithImagesLiveData

    fun getFramesWithImagesByFaceShapeId(faceShapeIds: List<Int>) {
        viewModelScope.launch {
            val framesWithImages = frameRepository.getFramesWithImagesByFaceShapeIds(faceShapeIds)
            _framesWithImagesLiveData.postValue(framesWithImages)
        }
    }

    fun getFramesForFaceShape(faceShape: String): LiveData<List<FrameWithImages>> {
        return repository.getFramesForFaceShape(faceShape)
    }

}