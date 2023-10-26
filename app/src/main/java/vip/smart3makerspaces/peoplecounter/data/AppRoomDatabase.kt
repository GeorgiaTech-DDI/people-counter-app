package vip.smart3makerspaces.peoplecounter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vip.smart3makerspaces.peoplecounter.data.dao.CountDao
import vip.smart3makerspaces.peoplecounter.data.dao.PersonDao
import vip.smart3makerspaces.peoplecounter.data.entity.Count
import vip.smart3makerspaces.peoplecounter.data.entity.Person

@Database(entities = [Count::class, Person::class], version = 1)
public abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun countDao(): CountDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
