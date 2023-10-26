package vip.smart3makerspaces.peoplecounter.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "count")
data class Count (
    @PrimaryKey val timestamp: Long,
    @ColumnInfo(name = "count") val count: Int
)