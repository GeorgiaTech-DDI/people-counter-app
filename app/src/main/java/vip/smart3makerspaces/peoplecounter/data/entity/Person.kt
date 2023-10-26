package vip.smart3makerspaces.peoplecounter.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person")
data class Person (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "confidence") val confidence: Float,
    @ColumnInfo(name = "left") val left: Float,
    @ColumnInfo(name = "top") val top: Float,
    @ColumnInfo(name = "right") val right: Float,
    @ColumnInfo(name = "bottom") val bottom: Float
)