package com.junkiedan.junkietuner.data.databases;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.junkiedan.junkietuner.data.dao.TuningDao;
import com.junkiedan.junkietuner.data.entities.Tuning;

/**
 * Room Database used by the application
 */
@Database(entities = {Tuning.class}, version = 1)
public abstract class JunkieTunerAppDatabase extends RoomDatabase {

    // Reference to the DAO
    public abstract TuningDao tuningDao();
    // The one and only active instance of the connection
    // with the database. This object is marked as volatile
    // because it will be accessed from different threads.
    private static volatile JunkieTunerAppDatabase instance = null;

    /**
     * Static method that returns (or first it creates and then retuens)
     * the active instance of the connection with the database.
     * @param context Main context of the application
     * @return The reference to the JunkieTunerAppDatabase
     */
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
