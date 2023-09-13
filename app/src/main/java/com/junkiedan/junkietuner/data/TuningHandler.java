package com.junkiedan.junkietuner.data;

import androidx.annotation.NonNull;

import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import com.junkiedan.junkietuner.util.notes.GuitarTuning;
import com.junkiedan.junkietuner.util.notes.Note;

import org.jetbrains.annotations.Contract;

public class TuningHandler {

    @NonNull
    @Contract("_ -> new")
    public static GuitarTuning getGuitarTuningFromTuning(@NonNull Tuning tuning) {
        String notesStr = tuning.notes.substring(1, tuning.notes.length() - 1);
        String[] notes = notesStr.split(",");
        assert notes.length != 0 : "Trying to create guitar tuning with 0 notes.";
        return new GuitarTuning(tuning.name, notes);
    }

    @NonNull
    public static String getNotesStringFromNotesArray(@NonNull Note[] notes) {
        StringBuilder notesStr = new StringBuilder();
        notesStr.append("[");
        for (Note note : notes) {
            notesStr.append(note.getName()).append(",");
        }
        notesStr.deleteCharAt(notesStr.length() - 1);
        notesStr.append("]");
        return notesStr.toString();
    }
}
