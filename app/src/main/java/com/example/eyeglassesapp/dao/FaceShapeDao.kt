package com.example.eyeglassesapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.eyeglassesapp.entities.FaceShapeEntity

@Dao
interface FaceShapeDao {
    @Insert
    suspend fun insertFaceShape(faceShape: FaceShapeEntity): Long

    @Query("SELECT * FROM face_shapes WHERE id = :id")
    suspend fun getFaceShapeById(id: Int): FaceShapeEntity?

    @Query("SELECT * FROM face_shapes")
    suspend fun getAllFaceShapes(): List<FaceShapeEntity>
}
