package com.junkiedan.junkietuner.core;

import android.content.Context;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Class contains reference to the Preferences DataStore data storage. For more information check
 * this <a href="https://developer.android.com/topic/libraries/architecture/datastore">link</a>
 * to the Android documentation. All methods are static inside this class (Therefore constructor
 * is empty and no instance of this class can be instantiated
 *
 * @author Stavros Barousis
 */
public class PreferencesDataStoreHandler {

    private static RxDataStore<Preferences> dataStore = null;
    private final static String DS_FILE_NAME = "settings";
    private final static Preferences.Key<Integer> CURRENT_TUNING_ID =
            PreferencesKeys.intKey("current_tuning_id");
    private final static Preferences.Key<Boolean> HAS_BEEN_INITIALIZED =
            PreferencesKeys.booleanKey("has_been_initialized");
    private final static Preferences.Key<Boolean> IS_TUNER_LOCKED =
            PreferencesKeys.booleanKey("is_tuner_locked");
    private final static Preferences.Key<Boolean> IS_LOAD_LAST_MUTED_STATE =
            PreferencesKeys.booleanKey("is_load_last_muted_state");
    private final static Preferences.Key<Boolean> IS_TUNING =
            PreferencesKeys.booleanKey("is_tuning");


    private PreferencesDataStoreHandler() {
        // Empty private constructor
    }

    /**
     * Datastore reference initializer (needs to be private)
     * @param context Current context that is passed as input
     */
    private static void initDataStore(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, DS_FILE_NAME).build();
    }

    /**
     * Retrieves data from the Preferences DataStore
     * @param context Current application context
     * @return Flowable object of the boolean value HAS_BEEN_INITIALIZED
     */
    public static Flowable<Boolean> hasBeenInitialized(Context context) {
        if (dataStore == null) {
            initDataStore(context);
        }
        return dataStore.data().map(preferences ->
                preferences.get(HAS_BEEN_INITIALIZED));
    }

    /**
     * Writes data to the Preferences DataStore
     * @param context Current application context
     * @param value Boolean value that will be stored on flag HAS_BEEN_INITIALIZED
     */
    public static void setHasBeenInitialized(Context context, boolean value) {
        if (dataStore == null) {
            initDataStore(context);
        }
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(HAS_BEEN_INITIALIZED, value);
            return Single.just(mutablePreferences);
        });
    }

    /**
     * Retrieves data from the Preferences DataStore
     * @param context Current application context
     * @return Flowable object of the integer value CURRENT_TUNING_ID
     */
    public static Flowable<Integer> getCurrentTuningId(Context context) {
        if (dataStore == null) {
            initDataStore(context);
        }
        return dataStore.data().map(preferences ->
                preferences.get(CURRENT_TUNING_ID));
    }

    /**
     * Writes data to the Preferences DataStore
     * @param context Current application context
     * @param newId Integer value that will be stored in CURRENT_TUNING_ID
     */
    public static void setCurrentTuningId(Context context, int newId) {
        if (dataStore == null) {
            initDataStore(context);
        }
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(CURRENT_TUNING_ID, newId);
            return Single.just(mutablePreferences);
        });
    }

    /**
     * Retrieves data from the Preferences DataStore
     * @param context Current application context
     * @return Flowable object of the boolean value IS_TUNER_LOCKED
     */
    public static Flowable<Boolean> getIsTunerLocked(Context context) {
        if (dataStore == null) {
            initDataStore(context);
        }
        return dataStore.data().map(preferences ->
                preferences.get(IS_TUNER_LOCKED));
    }

    /**
     * Writes data to the Preferences DataStore
     * @param context Current application context
     * @param value Boolean value that will be stored on flag IS_TUNER_LOCKED
     */
    public static void setIsTunerLocked(Context context, boolean value) {
        if (dataStore == null) {
            initDataStore(context);
        }
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(IS_TUNER_LOCKED, value);
            return Single.just(mutablePreferences);
        });
    }

    /**
     * Retrieves data from the Preferences DataStore
     * @param context Current application context
     * @return Flowable object of the boolean value IS_LOAD_LAST_MUTED_STATE
     */
    public static Flowable<Boolean> getIsLoadLastMutedState(Context context) {
        if (dataStore == null) {
            initDataStore(context);
        }
        return dataStore.data().map(preferences ->
                preferences.get(IS_LOAD_LAST_MUTED_STATE));
    }

    /**
     * Writes data to the Preferences DataStore
     * @param context Current application context
     * @param value Boolean value that will be stored on flag IS_LOAD_LAST_MUTED_STATE
     */
    public static void setIsLoadLastMutedState(Context context, boolean value) {
        if (dataStore == null) {
            initDataStore(context);
        }
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(IS_LOAD_LAST_MUTED_STATE, value);
            return Single.just(mutablePreferences);
        });
    }

    /**
     * Retrieves data from the Preferences DataStore
     * @param context Current application context
     * @return Flowable object of the boolean value IS_TUNING
     */
    public static Flowable<Boolean> getIsTuning(Context context) {
        if (dataStore == null) {
            initDataStore(context);
        }
        return dataStore.data().map(preferences ->
                preferences.get(IS_TUNING));
    }

    /**
     * Writes data to the Preferences DataStore
     * @param context Current application context
     * @param value Boolean value that will be stored on flag IS_TUNING
     */
    public static void setIsTuning(Context context, boolean value) {
        if (dataStore == null) {
            initDataStore(context);
        }
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(IS_TUNING, value);
            return Single.just(mutablePreferences);
        });
    }
}
