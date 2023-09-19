package com.junkiedan.junkietuner.data;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import com.junkiedan.junkietuner.data.dao.TuningDao;
import com.junkiedan.junkietuner.data.databases.JunkieTunerAppDatabase;
import com.junkiedan.junkietuner.data.entities.Tuning;
import java.util.List;

/**
 * Class that uses the Repository Design Pattern to perform the
 * queries in the Room Database asynchronously. The READ operations
 * store the information in LiveData objects and the WRITE, UPDATE and
 * DELETE operations are executed asynchronously using the doInBackground
 * method of objects that extend from AsyncTask<?, ?, ?> class.
 * @author Stavros Barousis
 */
public class TuningRepository {

    // Reference to the DAO
    private final TuningDao tuningDao;


    /**
     * Public constructor of the Repository class.
     * @param application The current Application object.
     */
    public TuningRepository(Application application) {
        JunkieTunerAppDatabase db = JunkieTunerAppDatabase.getDatabase(application);
        tuningDao = db.tuningDao();
    }

    public LiveData<List<Tuning>> getAllTunings() {
        return tuningDao.getAllTunings();
    }

    public LiveData<Tuning> getTuningById(int id) {
        return tuningDao.getTuningById(id);
    }

    public void insert(Tuning tuning) {
        new InsertAsyncTask(tuningDao).execute(tuning);
    }

    public void update(Tuning tuning) {
        new UpdateAsyncTask(tuningDao).execute(tuning);
    }

    public void deleteOne(Tuning tuning) {
        new DeleteAsyncTask(tuningDao).execute(tuning);
    }

    public void deleteAll() {
        new DeleteAsyncTask(tuningDao).execute();
    }

    private static class InsertAsyncTask extends AsyncTask<Tuning, Void, Void> {

        private final TuningDao asyncTaskDao;

        InsertAsyncTask(TuningDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Tuning... tunings) {
            asyncTaskDao.insertAll(tunings);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Tuning, Void, Void> {

        private final TuningDao asyncTaskDao;

        UpdateAsyncTask(TuningDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Tuning... tunings) {
            asyncTaskDao.update(tunings[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Tuning, Void, Void> {

        private final TuningDao asyncTaskDao;

        DeleteAsyncTask(TuningDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Tuning... tunings) {
            if (tunings == null || tunings.length == 0) {
                asyncTaskDao.deleteAll();
            } else {
                asyncTaskDao.delete(tunings[0]);
            }
            return null;
        }
    }

}
