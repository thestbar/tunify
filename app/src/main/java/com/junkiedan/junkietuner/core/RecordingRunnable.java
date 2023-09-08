package com.junkiedan.junkietuner.core;

import android.app.Activity;
import android.media.AudioRecord;
import android.widget.TextView;

import com.github.anastr.speedviewlib.SpeedView;
import com.junkiedan.junkietuner.util.algorithms.NoteDetection;
import com.junkiedan.junkietuner.util.algorithms.Yin;
import com.junkiedan.junkietuner.util.notes.Note;
import com.junkiedan.junkietuner.util.notes.NotesStructure;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecordingRunnable extends Thread {

    private final AtomicBoolean recordingInProgress;
    private final AudioRecord recorder;
    private final short[] inputBuffer;
    private final Yin yinInstance;
    private final NoteDetection noteDetection;
    private final NotesStructure notesStructure;
    private final TextView pitchTextView;
    private final Activity mainActivity;
    private final SpeedView speedView;

    public RecordingRunnable(Activity mainActivity, AtomicBoolean recordingInProgress,
                             AudioRecord recorder, TextView pitchTextView, SpeedView speedView,
                             short[] inputBuffer) {
        super();
        this.recordingInProgress = recordingInProgress;
        this.recorder = recorder;
        this.inputBuffer = inputBuffer;
        yinInstance = Yin.getInstance(recorder.getSampleRate());
        notesStructure = new NotesStructure();
        noteDetection = new NoteDetection(notesStructure);
        this.pitchTextView = pitchTextView;
        this.mainActivity = mainActivity;
        this.speedView = speedView;
    }

    @Override
    public void run() {
        int inputBufferLength = inputBuffer.length;

        while (recordingInProgress.get()) {
            recorder.read(inputBuffer, 0, inputBufferLength);
            double pitchInHz = yinInstance.getPitch(inputBuffer);
            if (pitchInHz == -1) {
                continue;
            }
            Note closestNote = noteDetection.findClosestNote(pitchInHz);
            double deltaInCents = NoteDetection.getDifferentInCents(closestNote, pitchInHz);

            mainActivity.runOnUiThread(() -> {
                pitchTextView.setText(closestNote.getName());
                speedView.speedTo(Math.round(deltaInCents), 300);
            });
        }
    }

}