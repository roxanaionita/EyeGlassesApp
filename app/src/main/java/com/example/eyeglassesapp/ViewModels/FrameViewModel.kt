package com.example.eyeglassesapp.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyeglassesapp.FrameWithImages
import com.example.eyeglassesapp.entities.FrameEntity
import com.example.eyeglassesapp.entities.FrameFaceShape_CrossEntity
import com.example.eyeglassesapp.repositories.FrameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FrameViewModel(private val repository: FrameRepository) : ViewModel() {

    //al frames with images for the admin frames page
    val allFramesWithImages: LiveData<List<FrameWithImages>> = repository.getAllFramesWithImages()

    private val _insertedFrameId = MutableLiveData<Long>()
    val insertedFrameId: LiveData<Long> = _insertedFrameId

    fun insertFrame(frame: FrameEntity) = viewModelScope.launch(Dispatchers.IO) {
        val newFrameId = repository.insertFrame(frame)
        _insertedFrameId.postValue(newFrameId)
    }
    fun resetInsertedFrameId() {
        _insertedFrameId.value = null
    }


    //functionalitati pentru main activity - RECYCLER VIEW FOR FRAMES cu pret mai mic decat limita
    private val _framesWithImageAndPriceLess = MutableLiveData<List<FrameWithImages>>()
    val framesWithImageAndPriceLess: LiveData<List<FrameWithImages>>
        get() = _framesWithImageAndPriceLess

    fun getFramesWithImageAndPriceLess() {
        Log.d("ViewModel", "Fetching frames...")
        viewModelScope.launch(Dispatchers.IO) {
            val frames = repository.getAllFramesWithImagesLessThanPrice(PRICE_LIMIT)
            Log.d("ViewModel", "Fetched frames: ${frames.size}")
            _framesWithImageAndPriceLess.postValue(frames)
        }
    }

    //limita de pret pentru main activity recycler view
    companion object {
        private const val PRICE_LIMIT = 700.0
    }


    //functionalitati pentru get Frames by CATEGORY
    private val _framesByCategory = MutableLiveData<List<FrameWithImages>>()
    val framesByCategory: LiveData<List<FrameWithImages>>
        get() = _framesByCategory

    fun getFramesByCategory(category: String) {
        Log.d("MainViewModel", "Fetching frames with category: $category...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val frames = repository.getFramesByCategory(category)
                Log.d("MainViewModel", "Fetched frames with category $category: ${frames.size}")
                _framesByCategory.postValue(frames)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching frames with category $category: ${e.message}", e)
                // Handle error appropriately (e.g., show error message)
            }
        }
    }

    //functionalitati pentru get frames by GENDER
    private val _framesByGender = MutableLiveData<List<FrameWithImages>>()
    val framesByGender: LiveData<List<FrameWithImages>>
        get() = _framesByGender

    fun getFramesByGender(category: String, gender: String) {
        Log.d("MainViewModel", "Fetching frames with gender: $gender...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val frames = repository.getFramesByGender(category,gender)
                Log.d("MainViewModel", "Fetched frames with gender $gender: ${frames.size}")
                _framesByGender.postValue(frames)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching frames with gender $gender: ${e.message}", e)
                // Handle error appropriately (e.g., show error message)
            }
        }
    }

    //frame with images for a specific frame id
    fun getFrameWithImagesById(frameId: Int): LiveData<FrameWithImages> {
        return repository.getFrameWithImagesById(frameId)
    }

    //get frame count
    private val _totalFrameCount = MutableLiveData<Int>()
    val totalFrameCount : LiveData<Int> = _totalFrameCount

    fun getTotalFrameCount(){
        viewModelScope.launch {
            try {
                val count = repository.getTotalFrameCount()
                _totalFrameCount.postValue(count)
            }catch (e:Exception){
                _totalFrameCount.postValue(0)
            }
        }
    }


    private val _eyeglassesCount = MutableLiveData<Int>()
    val eyeglassesCount: LiveData<Int> = _eyeglassesCount

    private val _sunglassesCount = MutableLiveData<Int>()
    val sunglassesCount: LiveData<Int> = _sunglassesCount

    fun fetchFrameCounts() {
        viewModelScope.launch {
            try {
                val eyeglassesCount = repository.getEyeglassesCount()
                _eyeglassesCount.postValue(eyeglassesCount)

                val sunglassesCount = repository.getSunglassesCount()
                _sunglassesCount.postValue(sunglassesCount)
            } catch (e: Exception) {
                _eyeglassesCount.postValue(0)
                _sunglassesCount.postValue(0)
            }
        }
    }



}