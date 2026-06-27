package com.ranielschneider.tvdetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Sessao::class, PontoGps::class],
    version = 1
)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao

    companion object {
        @Volatile
        private var INSTANCE: TrackerDatabase? = null

        fun getDatabase(context: Context): TrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackerDatabase::class.java,
                    "tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}