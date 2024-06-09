package com.example.eyeglassesapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(tableName = "frames")
data class FrameEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "frame_id") val frameId: Int = 0,
    @ColumnInfo(name = "brand") val brand: String,
    @ColumnInfo(name = "model") val model: String,
    @ColumnInfo(name = "colour") val colour: String,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "category") val category: String, // sunglasses or eyeglasses
    @ColumnInfo(name = "material") val material: String, // plastic, metal, etc
    @ColumnInfo(name = "frame_type") val frameType: String, // full, semi
)
