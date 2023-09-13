package com.junkiedan.junkietuner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.junkiedan.junkietuner.data.entities.Tuning;

import java.util.List;

@Dao
public interface TuningDao {
    @Query("SELECT * FROM Tuning")
    LiveData<List<Tuning>> getAllTunings();

    @Query("SELECT * FROM Tuning WHERE id=:id")
    LiveData<Tuning> getTuningById(int id);

    @Update
    void update(Tuning tuning);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Tuning... tunings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOne(Tuning tuning);

    @Delete
    void delete(Tuning tuning);

    @Query("DELETE FROM Tuning")
    void deleteAll();
}
