package vip.smart3makerspaces.peoplecounter.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vip.smart3makerspaces.peoplecounter.data.AppRoomRepository
import vip.smart3makerspaces.peoplecounter.data.entity.Count
import vip.smart3makerspaces.peoplecounter.data.entity.Person

class RecognitionViewModel(private val repository: AppRoomRepository) : ViewModel() {

    private val TAG = "RecognitionViewModel"

    val allCounts: LiveData<List<Count>> = repository.allCounts.asLiveData()
    val allPersons: LiveData<List<Person>> = repository.allPersons.asLiveData()

    fun insertCountToDatabase(count: Count) = viewModelScope.launch {
        repository.insertCount(count)
    }

    fun insertPersonsToDatabase(persons: List<Person>) = viewModelScope.launch {
        repository.insertPersons(persons)
    }

    fun insertPersonToDatabase(person: Person) = viewModelScope.launch {
        repository.insertPerson(person)
    }
}

class RecognitionViewModelFactory(private val repository: AppRoomRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecognitionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecognitionViewModel(repository) as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}