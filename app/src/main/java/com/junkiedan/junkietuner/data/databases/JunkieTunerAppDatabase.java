package com.junkiedan.junkietuner.data.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.junkiedan.junkietuner.data.dao.TuningDao;
import com.junkiedan.junkietuner.data.entities.Tuning;

@Database(entities = {Tuning.class}, version = 1)
public abstract class JunkieTunerAppDatabase extends RoomDatabase {
    public abstract TuningDao tuningDao();

    private static volatile JunkieTunerAppDatabase instance = null;

    public static JunkieTunerAppDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (JunkieTunerAppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    JunkieTunerAppDatabase.class, "app_db")
                            // Wipes and rebuilds instead of migrating
                            // if no Migration object.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
