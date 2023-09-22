package vip.smart3makerspaces.peoplecounter

import androidx.lifecycle.LiveData
import androidx.room.*

@Database(entities = [Count::class, Person::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun countDao(): CountDao
}

@Entity(tableName = "count")
data class Count (
    @PrimaryKey val time: Long,
    @ColumnInfo(name = "count") val count: Int
)

@Entity(tableName = "person")
data class Person (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "confidence") val confidence: Double,
    @ColumnInfo(name = "left") val left: Double,
    @ColumnInfo(name = "top") val top: Double,
    @ColumnInfo(name = "right") val right: Double,
    @ColumnInfo(name = "bottom") val bottom: Double
)

@Dao
interface CountDao {
    @Query("SELECT * FROM count ORDER BY time")
    fun getAll(): LiveData<List<Count>>

    @Insert
    fun insertAll(vararg counts: Count)

    @Delete
    fun delete(count: Count)
}