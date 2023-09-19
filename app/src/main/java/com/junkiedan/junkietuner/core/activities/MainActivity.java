package com.junkiedan.junkietuner.core.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.junkiedan.junkietuner.R;
import com.junkiedan.junkietuner.core.fragments.InfoFragment;
import com.junkiedan.junkietuner.core.fragments.MainFragment;
import com.junkiedan.junkietuner.core.PreferencesDataStoreHandler;
import com.junkiedan.junkietuner.core.fragments.SettingsFragment;
import com.junkiedan.junkietuner.core.fragments.TuningsFragment;
import com.junkiedan.junkietuner.data.TuningHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Main Parent Activity of the Application. All the basic initialization
 * of the application is performed here and all the fragments are displayed
 * and switched through this activity.
 * @author Stavros Barousis
 */
// TODO - Add search in the tunings list.
// TODO - Replace all blockingFirst to improve performance (if applicable).
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {android.Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_app_screen);

        initBottomNavBar();

        // Initialize the Database
        initDatabase();

        initSettings();

        askForPermissions();
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

    /**
     * Function that initializes the bottom navigation bar of the application.
     * Through the nav bar all the different fragments are displayed on the screen
     * of the application.
     */
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

    /**
     * Initializes the connection with the database of the application.
     * In case the database has never been initialized (For example
     * it is the 1st time that the application runs in a new device)
     * then the application automatically initializes it.
     */
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

    /**
     * Initializes the configuration settings  of the application.
     * In case the settings parameters have never been initialized
     * (For example it is the 1st time that the application runs
     * in a new device) then the application automatically initializes
     * them.
     */
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

    /**
     * The manager that transitions between the fragments of the application
     * when the users click to navigate to a different tab from the nav bar.
     * @param fragmentManager The fragment manager of the application.
     * @param targetFragmentClass The class of the new fragment that will be
     *                            displayed on the screen.
     * @param currentFragmentClass The current fragment that screen shows
     *                             to the user.
     * @param backStackName The back stage name which will be stored in the stack.
     * @return True if the transition is completed. False if the transition is
     * not completed. When the user click to navigate to the fragment that they
     * already are then the transition is not performed.
     */
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

    /**
     * Getter for the permission to record of the application.
     * @return True if the application has access to record, else
     * false.
     */
    public boolean isPermissionToRecordAccepted() {
        return permissionToRecordAccepted;
    }

    /**
     * Function that asks for permission on the device's microphone.
     */
    public void askForPermissions() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        System.out.println(Arrays.toString(permissions));
        System.out.println(isPermissionToRecordAccepted());
    }
}