package dev.thestbar.tunify.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.thestbar.tunify.data.entities.Tuning
import kotlinx.coroutines.flow.Flow

@Dao
interface TuningDao {

    @Query("SELECT * FROM Tuning")
    fun getAllTunings(): Flow<List<Tuning>>

    @Query("SELECT * FROM Tuning WHERE id = :id")
    fun getTuningById(id: Int): Flow<Tuning?>

    @Update
    suspend fun update(tuning: Tuning)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg tunings: Tuning)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(tuning: Tuning)

    @Delete
    suspend fun delete(tuning: Tuning)

    @Query("DELETE FROM Tuning")
    suspend fun deleteAll()
}
