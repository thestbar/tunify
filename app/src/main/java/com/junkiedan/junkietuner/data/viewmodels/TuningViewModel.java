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

    private TuningRepository tuningRepository;
    private LiveData<List<Tuning>> allTunings;

    public TuningViewModel(@NonNull Application application) {
        super(application);
        tuningRepository = new TuningRepository(application);
        allTunings = tuningRepository.getAllTunings();
    }

    public LiveData<List<Tuning>> getCurrentTunings() {
        return allTunings;
    }

    public void insert(Tuning tuning) {
        tuningRepository.insert(tuning);
    }

    public void update(Tuning tuning) {
        tuningRepository.update(tuning);
    }

    public void deleteOne(Tuning tuning) {
        tuningRepository.deleteOne(tuning);
    }

    public void deleteAll() {
        tuningRepository.deleteAll();
    }
}
