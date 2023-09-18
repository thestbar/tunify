package com.junkiedan.junkietuner.core;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class PreferencesDataStoreHandler {

    private static RxDataStore<Preferences> dataStore = null;
    private final static String DS_FILE_NAME = "settings";
    private final static Preferences.Key<Integer> CURRENT_TUNING_ID =
            PreferencesKeys.intKey("current_tuning_id");
    private final static Preferences.Key<Boolean> HAS_BEEN_INITIALIZED =
            PreferencesKeys.booleanKey("has_been_initialized");


    private PreferencesDataStoreHandler() {
        // Empty private constructor
    }

    private static void initDataStore(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, DS_FILE_NAME).build();
    }

    public static Flowable<Boolean> hasBeenInitialized(Context context) {
        if (dataStore == null) {
            initDataStore(context);
        }
        return dataStore.data().map(preferences ->
                preferences.get(HAS_BEEN_INITIALIZED));
    }

    public static void setHasBeenInitialized(Context context, boolean value) {
        if (dataStore == null) {
            initDataStore(context);
        }
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(HAS_BEEN_INITIALIZED, value);
            return Single.just(mutablePreferences);
        });
    }

    public static Flowable<Integer> getCurrentTuningId(Context context) {
        if (dataStore == null) {
            initDataStore(context);
        }
        return dataStore.data().map(preferences ->
                preferences.get(CURRENT_TUNING_ID));
    }

    public static void setCurrentTuningId(Context context, int newId) {
        if (dataStore == null) {
            initDataStore(context);
        }
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(CURRENT_TUNING_ID, newId);
            return Single.just(mutablePreferences);
        });
    }
}
