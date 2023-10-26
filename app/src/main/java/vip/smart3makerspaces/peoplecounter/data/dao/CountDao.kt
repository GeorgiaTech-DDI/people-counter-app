package vip.smart3makerspaces.peoplecounter.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import vip.smart3makerspaces.peoplecounter.data.entity.Count

@Dao
interface CountDao {
    @Query("SELECT * FROM count ORDER BY timestamp")
    fun getAll(): Flow<List<Count>>

    @Insert
    suspend fun insert(count: Count)

    @Delete
    suspend fun delete(count: Count)
}