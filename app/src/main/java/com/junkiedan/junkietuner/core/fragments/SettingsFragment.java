package com.junkiedan.junkietuner.core.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import com.junkiedan.junkietuner.R;
import com.junkiedan.junkietuner.core.PreferencesDataStoreHandler;
import com.junkiedan.junkietuner.data.TuningHandler;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * This fragment contains the settings of the application and ensures
 * that when they are changed then the new values are stored in the
 * Preferences DataStore.
 * @author Stavros Barousis
 */
public class SettingsFragment extends Fragment {

    // Reference to the 'Lock Tuner' Switch.
    private SwitchMaterial lockTunerSwitch;
    // Reference to the 'Load Last Muted State' Switch.
    private SwitchMaterial loadLastMutedStateSwitch;
    // Reference to 'Reset Tunings Database' TextView.
    private MaterialTextView resetDatabaseTextView;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize the references.
        lockTunerSwitch = view.findViewById(R.id.lockTunerSwitch);
        loadLastMutedStateSwitch = view.findViewById(R.id.loadLastMutedStateSwitch);
        resetDatabaseTextView = view.findViewById(R.id.resetDatabaseTextView);

        try {
            // Retrieve asynchronously the values from the database for values:
            // IS_TUNER_LOCKED
            PreferencesDataStoreHandler
                    .getIsTunerLocked(view.getContext())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(val -> {
                        lockTunerSwitch.setChecked(val);
                    })
                    .doOnError(t -> Log.println(Log.ERROR,
                            "SettingsFragment@PreferencesDataStoreHandler.getIsTunerLocked",
                            t.getMessage()))
                    .subscribe();
            // IS_LOAD_LAST_MUTED_STATE
            PreferencesDataStoreHandler
                    .getIsLoadLastMutedState(view.getContext())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(val -> {
                        loadLastMutedStateSwitch.setChecked(val);
                    })
                    .doOnError(t -> Log.println(Log.ERROR,
                            "SettingsFragment@PreferencesDataStoreHandler.getIsLoadLastMutedState",
                            t.getMessage()))
                    .subscribe();
        } catch (NullPointerException e) {
            Log.println(Log.ERROR, "SettingsFragment@onViewCreated",
                    "`IS_TUNER_LOCKED` or `IS_LOAD_LAST_MUTED_STATE` was not initialized");
        }

        // Set OnCheckedChangeListeners for all the Switches.
        lockTunerSwitch.setOnCheckedChangeListener((buttonView, isChecked)
                -> PreferencesDataStoreHandler
                .setIsTunerLocked(buttonView.getContext(), isChecked));

        loadLastMutedStateSwitch.setOnCheckedChangeListener((buttonView, isChecked)
                -> PreferencesDataStoreHandler
                .setIsLoadLastMutedState(buttonView.getContext(), isChecked));

        // 'Reset Tunings Database' functionality is initiated.
        resetDatabaseTextView.setClickable(true);
        resetDatabaseTextView.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(v.getContext())
                .setTitle("Reset Tuning Database")
                .setMessage("All the changes that you made will be lost. The database will " +
                        "contain only the initial tunings. Do you still want to proceed?")
                .setPositiveButton("No", (dialog, which) -> {
                    // Do nothing
                    dialog.dismiss();
                })
                .setNegativeButton("Yes", (dialog, which) -> {
                    Log.println(Log.WARN, "SettingsFragment@onViewCreated",
                            "APP DATABASE RESET");
                    TuningHandler.resetDatabaseValuesToDefault(requireActivity().getApplication());
                })
                .show());
    }
}