package com.example.eyeglassesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])] 
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "order_id") val orderId: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "num_products") val totalNumberProducts: Int,
    @ColumnInfo(name = "order_date") val orderDate: Date,
    @ColumnInfo(name = "total_price") val totalPrice: Double
)
