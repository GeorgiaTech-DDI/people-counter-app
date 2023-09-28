package vip.smart3makerspaces.peoplecounter

import androidx.lifecycle.LiveData
import androidx.room.*

@Database(entities = [Count::class, Person::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun countDao(): CountDao
}

@Entity(tableName = "count")
data class Count (
    @PrimaryKey val timestamp: Long,
    @ColumnInfo(name = "count") val count: Int
)

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

@Dao
interface CountDao {
    @Query("SELECT * FROM count ORDER BY timestamp")
    fun getAll(): LiveData<List<Count>>

    @Insert
    fun insertAll(vararg counts: Count)

    @Delete
    fun delete(count: Count)
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM person")
    fun getAll(): LiveData<List<Person>>

    @Insert
    fun insertAll(vararg person: Person)

    @Delete
    fun delete(person: Person)
}
