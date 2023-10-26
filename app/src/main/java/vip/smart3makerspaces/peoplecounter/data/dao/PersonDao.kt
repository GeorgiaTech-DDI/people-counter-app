package vip.smart3makerspaces.peoplecounter.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import vip.smart3makerspaces.peoplecounter.data.entity.Person

@Dao
interface PersonDao {
    @Query("SELECT * FROM person")
    fun getAll(): Flow<List<Person>>

    @Insert
    suspend fun insertAll(persons: List<Person>)

    @Insert
    suspend fun insert(person: Person)
    @Delete
    suspend fun delete(person: Person)
}
