package vip.smart3makerspaces.peoplecounter.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import vip.smart3makerspaces.peoplecounter.data.dao.CountDao
import vip.smart3makerspaces.peoplecounter.data.dao.PersonDao
import vip.smart3makerspaces.peoplecounter.data.entity.Count
import vip.smart3makerspaces.peoplecounter.data.entity.Person

class AppRoomRepository(private val countDao: CountDao, private val personDao: PersonDao) {

    val allCounts: Flow<List<Count>> = countDao.getAll()
    val allPersons: Flow<List<Person>> = personDao.getAll()

    @WorkerThread
    suspend fun insertCount(count: Count) {
        countDao.insert(count)
    }

    @WorkerThread
    suspend fun insertPersons(persons: List<Person>) {
        personDao.insertAll(persons)
    }

    @WorkerThread
    suspend fun insertPerson(person: Person) {
        personDao.insert(person)
    }
}