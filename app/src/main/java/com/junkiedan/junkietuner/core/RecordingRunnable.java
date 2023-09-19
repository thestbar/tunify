package com.junkiedan.junkietuner.core;

import static com.junkiedan.junkietuner.core.fragments.MainFragment.NEEDLE_ANIMATION_SPEED;
import android.app.Activity;
import android.media.AudioRecord;
import android.widget.TextView;
import com.github.anastr.speedviewlib.SpeedView;
import com.junkiedan.junkietuner.util.algorithms.NoteDetection;
import com.junkiedan.junkietuner.util.algorithms.Yin;
import com.junkiedan.junkietuner.util.notes.Note;
import com.junkiedan.junkietuner.util.notes.NotesStructure;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that represents the thread that is responsible
 * for all the functionalities that run when the application
 * is recording audio for tuning.
 *
 * @author Stavros Barousis
 */
public class RecordingRunnable extends Thread {

    // Atomic boolean flag accessed from different threads of the application
    private final AtomicBoolean recordingInProgress;
    // The reference to the Audio Recorder object
    private final AudioRecord recorder;
    // Short input buffer where the raw input from microphone is written
    private final short[] inputBuffer;
    // Instance of the Yin pitch detection algorithm
    private final Yin yinInstance;
    // Note detection schema reference. This value is static because it might
    // change while the recording is live. The recording starts and in case the
    // tuning is unlocked then the default NotesStructure.getAllNotes() is used
    // but in case the tuning process is locked and a specific tuning is selected
    // it is retrieved from the database and as soon as it's value is returned
    // to the main thread, then note detection changes asynchronously
    private static NoteDetection noteDetection;
    // Reference to the text view that displays the closest note to the current pitch
    private final TextView pitchTextView;
    // Reference to the main activity of the application. This is mandatory to run
    // changes on the UI (because changes on UI can happen only from the main thread)
    private final Activity mainActivity;
    // Reference to the speed view item (In order to update needle's position)
    private final SpeedView speedView;

    /**
     * Public constructor initiates the record running instance in a new thread.
     * @param mainActivity Reference to the main activity of the application.
     * @param recordingInProgress Reference to the flag that indicates if a recording is
     *                            currently live.
     * @param recorder Reference to AudioRecorder object.
     * @param pitchTextView Reference to the TextView object that displays the value
     *                      of the note that is closest to the current pitch that is
     *                      evaluated from the pitch detection algorithm.
     * @param speedView Reference to SpeedView object.
     * @param inputBuffer The input buffer the RAW input from microphone is stored.
     */
    public RecordingRunnable(Activity mainActivity, AtomicBoolean recordingInProgress,
                             AudioRecord recorder, TextView pitchTextView, SpeedView speedView,
                             short[] inputBuffer) {
        super();
        this.recordingInProgress = recordingInProgress;
        this.recorder = recorder;
        this.inputBuffer = inputBuffer;
        yinInstance = Yin.getInstance(recorder.getSampleRate());
        if (noteDetection == null) {
            noteDetection = new NoteDetection(NotesStructure.getAllNotes());
        }
        this.pitchTextView = pitchTextView;
        this.mainActivity = mainActivity;
        this.speedView = speedView;
    }

    @Override
    public void run() {
        int inputBufferLength = inputBuffer.length;

        // Do while recording is alive
        while (recordingInProgress.get()) {
            // Write audio data to inputBuffer
            recorder.read(inputBuffer, 0, inputBufferLength);
            // Calculate the pitch of this input
            double pitchInHz = yinInstance.getPitch(inputBuffer);
            // Check if the pitch that is returned has a logical value
            if (!Double.isFinite(pitchInHz) || pitchInHz == -1) {
                continue;
            }
            // Find closest note to that pitch (based on the selected note
            // detection pattern that is selected)
            Note closestNote = noteDetection.findClosestNote(pitchInHz);
            // Calculate the distance from the note in Cents
            double deltaInCents = NoteDetection.getDifferentInCents(closestNote, pitchInHz);

            // Perform the changes on the User Interface
            mainActivity.runOnUiThread(() -> {
                pitchTextView.setText(closestNote.getName());
                speedView.speedTo(Math.round(deltaInCents), NEEDLE_ANIMATION_SPEED);
            });
        }
    }

    /**
     * Static function that changes the Note Detection scheme.
     * @param newNoteDetection The new Note Detection scheme that will be selected
     *                         for the current instance of the RecordingRunnable.
     */
    public static synchronized void setNoteDetection(NoteDetection newNoteDetection) {
        noteDetection = newNoteDetection;
    }

}