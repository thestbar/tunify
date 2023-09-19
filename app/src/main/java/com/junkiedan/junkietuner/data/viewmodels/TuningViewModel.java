package com.junkiedan.junkietuner.data.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.junkiedan.junkietuner.data.TuningRepository;
import com.junkiedan.junkietuner.data.entities.Tuning;
import java.util.List;

/**
 * The view model of the database that is the abstract layer that is used
 * by the application to communicate with the database.
 * <b>WARNING</b>:
 * Never pass context into ViewModel instances. Do not store Activity, Fragment,
 * or View instances or their Context in the ViewModel. An Activity can be destroyed
 * and created many times during the lifecycle of a ViewModel, such as when the
 * device is rotated. If you store a reference to the Activity in the ViewModel,
 * you end up with references that point to the destroyed Activity. This is a
 * memory leak. If you need the application context, use AndroidViewModel instead
 * of ViewModel.
 * @author Stavros Barousis
 */
public class TuningViewModel extends AndroidViewModel {

    // Reference to the TuningRepository of the database.
    private static TuningRepository tuningRepository;
    // The live data object that contains the list of
    // all the available tunings of the application.
    // Might be unnecessary.
    private static LiveData<List<Tuning>> allTunings;
    private static TuningViewModel instance = null;

    // Private constructor so there is only one active
    // instance of this class.
    private TuningViewModel(@NonNull Application application) {
        super(application);
        tuningRepository = new TuningRepository(application);
        allTunings = tuningRepository.getAllTunings();
    }

    /**
     * Performs Query "SELECT * FROM Tunings;"
     * @param application The reference to the Application Object.
     * @return The list of tunings wrapped in a live data object.
     */
    public static LiveData<List<Tuning>> getCurrentTunings(Application application) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        return allTunings;
    }

    /**
     * Performs Query "SELECT * FROM Tunings WHERE id={ID_PARAM};"
     * @param application The reference to the Application Object.
     * @param id The unique database ID of the tuning object.
     * @return The tuning wrapped in a live data object.
     */
    public static LiveData<Tuning> getTuningById(Application application, int id) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        return tuningRepository.getTuningById(id);
    }

    /**
     * Performs Query "INSERT INTO Tunings VALUES {TUNING_PARAM_VALUES};"
     * @param application The reference to the Application Object.
     * @param tuning The tuning object that will be stored in the database.
     */
    public static void insert(Application application, Tuning tuning) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        tuningRepository.insert(tuning);
    }

    /**
     * Performs Query "UPDATE Tunings SET {TUNING_PARAM_VALUES} WHERE id={TUNING_PARAM_ID};"
     * @param application The reference to the Application Object.
     * @param tuning The tuning object that will be updated in the database.
     */
    public static void update(Application application, Tuning tuning) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        tuningRepository.update(tuning);
    }

    /**
     * Performs Query "DELETE FROM Tunings WHERE id={TUNING_PARAM_ID};"
     * @param application The reference to the Application Object.
     * @param tuning The tuning object that will be deleted from the database.
     */
    public static void deleteOne(Application application, Tuning tuning) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        tuningRepository.deleteOne(tuning);
    }

    /**
     * Performs Query "DELETE FROM Tunings;"
     * @param application The reference to the Application Object.
     */
    public static void deleteAll(Application application) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        tuningRepository.deleteAll();
    }
}
