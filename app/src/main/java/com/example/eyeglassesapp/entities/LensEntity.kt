package com.example.eyeglassesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lenses")
data class LensEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "lens_id") val lensId: Int = 0,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "material") val material: String,
    @ColumnInfo(name = "pc_filter") val pcFilter: Boolean,
    @ColumnInfo(name = "uv_filter") val uvFilter: Boolean,
    @ColumnInfo(name = "price") val price: Double
)