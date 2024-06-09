package com.example.eyeglassesapp

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RemoveBgApi {
    @Multipart
    @POST("v1.0/removebg")
    @Headers("X-Api-Key: icJRzZ6kesgrmR5KXynwbKT5")
    fun removeBackground(
        @Part image : MultipartBody.Part
    ) : Call<ResponseBody>
}