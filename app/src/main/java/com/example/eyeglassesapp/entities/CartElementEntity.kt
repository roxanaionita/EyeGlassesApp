package com.example.eyeglassesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_elements")
data class CartElementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "pair_id") val pairId: Long,
    @ColumnInfo(name = "quantity") var quantity: Int,
    @ColumnInfo(name = "price") val price: Double
)