package com.junkiedan.junkietuner.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.junkiedan.junkietuner.data.TuningRepository;
import com.junkiedan.junkietuner.data.entities.Tuning;

import java.util.List;

/**
 * <b>WARNING</b>:
 * Never pass context into ViewModel instances. Do not store Activity, Fragment,
 * or View instances or their Context in the ViewModel. An Activity can be destroyed
 * and created many times during the lifecycle of a ViewModel, such as when the
 * device is rotated. If you store a reference to the Activity in the ViewModel,
 * you end up with references that point to the destroyed Activity. This is a
 * memory leak. If you need the application context, use AndroidViewModel instead
 * of ViewModel.
 */
public class TuningViewModel extends AndroidViewModel {

    private static TuningRepository tuningRepository;
    private static LiveData<List<Tuning>> allTunings;
    private static TuningViewModel instance = null;

    private TuningViewModel(@NonNull Application application) {
        super(application);
        tuningRepository = new TuningRepository(application);
        allTunings = tuningRepository.getAllTunings();
    }

    public static LiveData<List<Tuning>> getCurrentTunings(Application application) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        return allTunings;
    }

    public static LiveData<Tuning> getTuningById(Application application, int id) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        return tuningRepository.getTuningById(id);
    }

    public static void insert(Application application, Tuning tuning) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        tuningRepository.insert(tuning);
    }

    public static void update(Application application, Tuning tuning) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        tuningRepository.update(tuning);
    }

    public static void deleteOne(Tuning tuning) {
        tuningRepository.deleteOne(tuning);
    }

    public static void deleteAll(Application application) {
        if (instance == null) {
            instance = new TuningViewModel(application);
        }
        tuningRepository.deleteAll();
    }
}
