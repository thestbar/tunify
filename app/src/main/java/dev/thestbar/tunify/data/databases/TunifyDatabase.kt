package dev.thestbar.tunify.data.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.thestbar.tunify.data.dao.TuningDao
import dev.thestbar.tunify.data.entities.Tuning

@Database(entities = [Tuning::class], version = 1, exportSchema = false)
abstract class TunifyDatabase : RoomDatabase() {

    abstract fun tuningDao(): TuningDao

    companion object {
        @Volatile
        private var INSTANCE: TunifyDatabase? = null

        fun getDatabase(context: Context): TunifyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TunifyDatabase::class.java,
                    "app_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
