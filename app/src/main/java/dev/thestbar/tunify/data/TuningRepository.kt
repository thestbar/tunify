package dev.thestbar.tunify.data

import android.app.Application
import dev.thestbar.tunify.data.dao.TuningDao
import dev.thestbar.tunify.data.databases.TunifyDatabase
import dev.thestbar.tunify.data.entities.Tuning
import kotlinx.coroutines.flow.Flow

class TuningRepository(application: Application) {

    private val tuningDao: TuningDao =
        TunifyDatabase.getDatabase(application).tuningDao()

    fun getAllTunings(): Flow<List<Tuning>> = tuningDao.getAllTunings()

    fun getTuningById(id: Int): Flow<Tuning?> = tuningDao.getTuningById(id)

    suspend fun insert(tuning: Tuning) = tuningDao.insertOne(tuning)

    suspend fun update(tuning: Tuning) = tuningDao.update(tuning)

    suspend fun delete(tuning: Tuning) = tuningDao.delete(tuning)

    suspend fun deleteAll() = tuningDao.deleteAll()
}
