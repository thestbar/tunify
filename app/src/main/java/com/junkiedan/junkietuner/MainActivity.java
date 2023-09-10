package com.junkiedan.junkietuner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.TextView;

import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.junkiedan.junkietuner.core.RecordingRunnable;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends AppCompatActivity {

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
    private TextView pitchTextView = null;
    private SwitchMaterial tuningSwitch = null;
    private final static String SWITCH_TURNED_ON_STR = "Tuning";
    private final static String SWITCH_TURNED_OFF_STR = "Muted";
    private SpeedView speedView = null;
    public final static long NEEDLE_ANIMATION_SPEED = 300;

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

        tuningSwitch = findViewById(R.id.tuningSwitch);
        tuningSwitch.setChecked(false);

        tuningSwitch.setOnClickListener(v -> {
            if (tuningSwitch.isChecked()) {
                startRecording();
            } else {
                stopRecording();
            }
        });

        pitchTextView = findViewById(R.id.textViewPitch);
        pitchTextView.setText("");

        initSpeedView();

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tuningSwitch.setChecked(false);
        stopRecording();
    }

    @Override
    protected void onPause() {
        super.onPause();
        tuningSwitch.setChecked(false);
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

    private void initSpeedView() {
        speedView = findViewById(R.id.speedView);

        // Speedometer limits
        speedView.setMinSpeed(-50);
        speedView.setMaxSpeed(50);

        // Section attributes
        speedView.clearSections();
        int speedViewSectionColorId = ContextCompat.getColor(this, R.color.custom_vanilla);
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

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
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
                        this,
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