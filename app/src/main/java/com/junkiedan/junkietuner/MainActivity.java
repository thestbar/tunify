package com.junkiedan.junkietuner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.junkiedan.junkietuner.core.PreferencesDataStoreHandler;
import com.junkiedan.junkietuner.data.TuningHandler;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {android.Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_app_screen);

        initBottomNavBar();

        // Initialize the Database
        // TODO - Add search in the tunings list
        // TODO - Replace all blockingFirst to improve performance

        initDatabase();

        initSettings();

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void initBottomNavBar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener( item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            List<Fragment> allFragments = fragmentManager.getFragments();
            Class<? extends Fragment> currentFragmentClass =
                    allFragments.get(allFragments.size() - 1).getClass();
            if (item.getItemId() == R.id.page_1) {
                return transitionFragment(fragmentManager, MainFragment.class,
                        currentFragmentClass, "Main Screen");
            } else if (item.getItemId() == R.id.page_2) {
                return transitionFragment(fragmentManager, TuningsFragment.class,
                        currentFragmentClass, "Tunings Screen");
            } else if (item.getItemId() == R.id.page_3) {
                return transitionFragment(fragmentManager, SettingsFragment.class,
                        currentFragmentClass, "Settings Screen");
            } else if (item.getItemId() == R.id.page_4) {
                return transitionFragment(fragmentManager, InfoFragment.class,
                        currentFragmentClass, "Info Screen");
            }
            return false;
        });
    }

    private void initDatabase() {
        // Reset DB only if it is the first time that the application opens
        try {
            boolean dbHasBeenInitialized = PreferencesDataStoreHandler
                    .hasBeenInitialized(getApplicationContext())
                    .blockingFirst();
            if (!dbHasBeenInitialized) {
                TuningHandler.resetDatabaseValuesToDefault(getApplication());
            }
        } catch (NullPointerException e) {
            Log.println(Log.WARN, "MainActivity@onCreate", "NullPointerException " +
                    "fired when trying to retrieve hasBeenInitialized value from prefs");
            TuningHandler.resetDatabaseValuesToDefault(getApplication());
            PreferencesDataStoreHandler.setHasBeenInitialized(getApplicationContext(), true);
        }
    }

    private void initSettings() {
        // Set settings values in case they are not stored
        // IS_TUNER_LOCKED
        try {
            boolean isTunerLocked = PreferencesDataStoreHandler
                    .getIsTunerLocked(getApplicationContext())
                    .blockingFirst();
            Log.println(Log.DEBUG, "IS_TUNER_LOCKED", String.valueOf(isTunerLocked));
        } catch (NullPointerException e) {
            Log.println(Log.DEBUG, "IS_TUNER_LOCKED", "Value was not initialized." +
                    " Set to `false`");
            PreferencesDataStoreHandler.setIsTunerLocked(getApplicationContext(), false);
        }

        // IS_TUNER_LOCKED
        try {
            boolean isLoadLastMutedState = PreferencesDataStoreHandler
                    .getIsLoadLastMutedState(getApplicationContext())
                    .blockingFirst();
            Log.println(Log.DEBUG, "IS_LOAD_LAST_MUTED_STATE",
                    String.valueOf(isLoadLastMutedState));
        } catch (NullPointerException e) {
            Log.println(Log.DEBUG, "IS_LOAD_LAST_MUTED_STATE",
                    "Value was not initialized. Set to `true`");
            PreferencesDataStoreHandler
                    .setIsLoadLastMutedState(getApplicationContext(), true);
        }

        // IS_MUTED
        try {
            boolean isTuning = PreferencesDataStoreHandler
                    .getIsTuning(getApplicationContext())
                    .blockingFirst();
            Log.println(Log.DEBUG, "IS_TUNING", String.valueOf(isTuning));
        } catch (NullPointerException e) {
            Log.println(Log.DEBUG, "IS_TUNING",
                    "Value was not initialized. Set to `true`");
            PreferencesDataStoreHandler.setIsTuning(getApplicationContext(), true);
        }
    }

    private boolean transitionFragment(FragmentManager fragmentManager,
                                    Class<? extends Fragment> targetFragmentClass,
                                    Class<? extends Fragment> currentFragmentClass,
                                    String backStackName) {
        if (currentFragmentClass == targetFragmentClass) {
            return false;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, targetFragmentClass, null)
                .setReorderingAllowed(true)
                .addToBackStack(backStackName)
                .commit();
        return true;
    }

    public boolean isPermissionToRecordAccepted() {
        return permissionToRecordAccepted;
    }

    public String[] getPermissions() {
        return permissions;
    }
}