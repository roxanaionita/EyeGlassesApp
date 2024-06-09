package com.example.eyeglassesapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

fun removeBackgroundFromImage(imageBitmap: Bitmap, callback: (Bitmap?) -> Unit) {
    val byteArrayOutputStream = ByteArrayOutputStream()
    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val requestBody = RequestBody.create("image/png".toMediaTypeOrNull(), byteArrayOutputStream.toByteArray())
    val body = MultipartBody.Part.createFormData("image_file", "image.png", requestBody)

    val call = RetrofitClient.instance.removeBackground(body)
    call.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                response.body()?.byteStream()?.let { inputStream ->
                    val resultBitmap = BitmapFactory.decodeStream(inputStream)
                    callback(resultBitmap)
                }
            } else {
                Log.e("RemoveBg", "Error: ${response.errorBody()?.string()}")
                callback(null)
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("RemoveBg", "Failed to remove background", t)
            callback(null)
        }
    })
}