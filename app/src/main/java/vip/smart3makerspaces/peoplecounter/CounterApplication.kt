package vip.smart3makerspaces.peoplecounter

import android.app.Application
import vip.smart3makerspaces.peoplecounter.data.AppRoomDatabase
import vip.smart3makerspaces.peoplecounter.data.AppRoomRepository

class CounterApplication : Application() {

    val database by lazy { AppRoomDatabase.getDatabase(this) }
    val roomRepository by lazy { AppRoomRepository(database.countDao(), database.personDao()) }
}