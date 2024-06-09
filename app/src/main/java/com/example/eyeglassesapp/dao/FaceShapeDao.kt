package com.example.eyeglassesapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eyeglassesapp.entities.FaceShapeEntity

@Dao
interface FaceShapeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFaceShape(faceShape: FaceShapeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(faceShapes: List<FaceShapeEntity>)

    @Query("SELECT * FROM face_shapes WHERE face_shape_id = :id")
    suspend fun getFaceShapeById(id: Int): FaceShapeEntity?

    @Query("SELECT * FROM face_shapes")
    suspend fun getAllFaceShapes(): List<FaceShapeEntity>
    @Query("SELECT face_shape_id FROM face_shapes WHERE face_shape = :faceShape")
    fun getFaceShapeIdByName(faceShape: String): Int
}
