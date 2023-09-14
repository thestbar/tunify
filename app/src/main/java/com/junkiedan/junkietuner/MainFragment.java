package com.junkiedan.junkietuner;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.junkiedan.junkietuner.core.PreferencesDataStoreHandler;
import com.junkiedan.junkietuner.core.RecordingRunnable;
import com.junkiedan.junkietuner.data.TuningHandler;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import com.junkiedan.junkietuner.util.notes.GuitarTuning;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import kotlinx.coroutines.flow.Flow;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int SAMPLING_RATE_IN_HZ = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * Factor by that the minimum buffer size is multiplied. The bigger the factor is the less
     * likely it is that samples will be dropped, but more memory will be used. The minimum buffer
     * size is determined by {@link AudioRecord#getMinBufferSize(int, int, int)} and depends on the
     * recording settings.
     */
    private static final int BUFFER_SIZE_FACTOR = 4;
    /**
     * Size of the buffer where the audio data is stored by Android
     */
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR;
    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private short[] buffer = null;
    private SwitchMaterial tuningSwitch = null;
    private TextView pitchTextView = null;
    //    private SwitchMaterial tuningSwitch = null;
    private final static String SWITCH_TURNED_ON_STR = "Tuning";
    private final static String SWITCH_TURNED_OFF_STR = "Muted";
    private SpeedView speedView = null;
    public final static long NEEDLE_ANIMATION_SPEED = 300;

    private boolean permissionToRecordAccepted;
    private String[] permissions;

    private List<TextView> notesTextViewList;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = ((MainActivity) requireActivity()).getPermissions();
        permissionToRecordAccepted = ((MainActivity) requireActivity()).isPermissionToRecordAccepted();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTuningSwitch();
        initPitchTextView();
        initSpeedView();
        // First get references to the notes text views of the main fragment
        initNotesTextViewList();
        // Then initialize the string values
        initSelectedTuning();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tuningSwitch != null) {
            tuningSwitch.setChecked(false);
        }
        stopRecording();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tuningSwitch != null) {
            tuningSwitch.setChecked(false);
        }
        stopRecording();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    private void initTuningSwitch() {
        tuningSwitch = requireView().findViewById(R.id.tuningSwitch);
        tuningSwitch.setChecked(false);

        tuningSwitch.setOnClickListener(v -> {
            if (tuningSwitch.isChecked()) {
                startRecording();
            } else {
                stopRecording();
            }
        });
    }

    private void initPitchTextView() {
        pitchTextView = requireView().findViewById(R.id.textViewPitch);
        pitchTextView.setText("");
    }

    private void initSpeedView() {
        speedView = requireView().findViewById(R.id.speedView);

        // Speedometer limits
        speedView.setMinSpeed(-50);
        speedView.setMaxSpeed(50);

        // Section attributes
        speedView.clearSections();
        int speedViewSectionColorId = ContextCompat.getColor(requireContext(), R.color.custom_vanilla);
        Section mainSection = new Section(0f, 1f, speedViewSectionColorId);
        mainSection.setStyle(Style.ROUND);
        speedView.addSections(mainSection);
        speedView.setSpeedometerWidth(8);

        // Marks attributes
        speedView.setMarksNumber(9);
        speedView.setMarkStyle(Style.ROUND);
        speedView.setMarksPadding(5);
        speedView.setMarkHeight(10);

        // Tick attributes
        speedView.setTickNumber(11);
        speedView.setTickPadding(20);
    }

    private void initSelectedTuning() {
        // Retrieve current tuning id from preferences
        Flowable<Integer> currentTuningIdFlowable = PreferencesDataStoreHandler
                .getCurrentTuningId(requireContext());
        // In case no initiation has been provided then
        // the flowable will be null and the program
        // has to take care of it
        // Extract tuning id value
        int currentTuningId;
        try {
            currentTuningId = currentTuningIdFlowable.blockingFirst();
        } catch (NullPointerException e) {
            Log.println(Log.WARN, "MainFragment@initSelectedTuning", "When " +
                    "trying to retrieve currentTuningIdFlowable.blockingFirst() a " +
                    "NullPointerException was fired because the value has not been " +
                    "initialized in PreferencesDataStore. The value has been set to -1");
            currentTuningId = -1;
        }
        // Check if tuning exists in database
        LiveData<Tuning> currentTuning =
                TuningViewModel.getTuningById(requireActivity().getApplication(), currentTuningId);
        final Observer<Tuning> observer = tuning -> {
//            Log.println(Log.DEBUG, "Current Tuning",
//                    tuning == null ? "null" : tuning.toString());
            // In case tuning is null, this means that the selected tuning
            // does not exist in the database anymore. Therefore, it will
            // be initialized to Standard E tuning
            if (tuning == null) {
                tuning = new Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]");
            }
            // Extract guitar tuning from tuning
            GuitarTuning guitarTuning = TuningHandler.getGuitarTuningFromTuning(tuning);
            // Set notes on text views
            int i, len = notesTextViewList.size();
            for (i = 0; i < len; ++i) {
                notesTextViewList.get(i)
                        .setText(guitarTuning.getNotes()[i].getName());
            }
        };
        currentTuning.observe(getViewLifecycleOwner(), observer);
    }

    private void initNotesTextViewList() {
        notesTextViewList = new ArrayList<>();
        notesTextViewList.add(requireView().findViewById(R.id.textViewNote1));
        notesTextViewList.add(requireView().findViewById(R.id.textViewNote2));
        notesTextViewList.add(requireView().findViewById(R.id.textViewNote3));
        notesTextViewList.add(requireView().findViewById(R.id.textViewNote4));
        notesTextViewList.add(requireView().findViewById(R.id.textViewNote5));
        notesTextViewList.add(requireView().findViewById(R.id.textViewNote6));
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }
        buffer = new short[BUFFER_SIZE];
        recorder = new AudioRecord(MediaRecorder.AudioSource.UNPROCESSED,
                SAMPLING_RATE_IN_HZ,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE);
        recorder.startRecording();
        recordingInProgress.set(true);
        recordingThread = new Thread(
                new RecordingRunnable(
                        requireActivity(),
                        recordingInProgress,
                        recorder,
                        pitchTextView,
                        speedView,
                        buffer),
                "Recording Thread");
        recordingThread.start();
        tuningSwitch.setText(SWITCH_TURNED_ON_STR);
    }

    private void stopRecording() {
        if(recorder == null) {
            return;
        }
        recordingInProgress.set(false);
        recorder.stop();
        recorder.release();
        recorder = null;
        recordingThread = null;
        pitchTextView.setText("");
        tuningSwitch.setText(SWITCH_TURNED_OFF_STR);
        speedView.speedTo(0, NEEDLE_ANIMATION_SPEED);
    }
}