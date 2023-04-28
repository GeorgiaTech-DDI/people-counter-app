package vip.smart3makerspaces.peoplecounter

import androidx.lifecycle.LiveData
import androidx.room.*

@Database(entities = [PersonCount::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personCountDao(): PersonCountDao
}

@Entity(tableName = "person_count")
data class PersonCount (
    @PrimaryKey val time: Long,
    @ColumnInfo(name = "count") val count: Int
)

@Dao
interface PersonCountDao {
    @Query("SELECT * FROM person_count ORDER BY time")
    fun getAll(): LiveData<List<PersonCount>>

    @Insert
    fun insertAll(vararg counts: PersonCount)

    @Delete
    fun delete(count: PersonCount)
}