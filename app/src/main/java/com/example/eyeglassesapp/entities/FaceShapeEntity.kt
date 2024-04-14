package com.example.eyeglassesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "face_shapes")
data class FaceShapeEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "face_shape") val faceShape: String, // can be round, oval, square, oblong and heart
    @ColumnInfo(name = "description") val description : String
)
