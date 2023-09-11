package com.junkiedan.junkietuner.data;

import androidx.annotation.NonNull;

import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import com.junkiedan.junkietuner.util.notes.GuitarTuning;
import com.junkiedan.junkietuner.util.notes.Note;
import com.junkiedan.junkietuner.util.notes.NotesStructure;

import org.jetbrains.annotations.Contract;

public class TuningHandler {
    public static void insertTuning(@NonNull TuningViewModel tuningViewModel,
                                    String tuningName, Note[] notes) {
        Tuning tuning = new Tuning(tuningName, getNotesStringFromNotesArray(notes));
        tuningViewModel.insert(tuning);
    }

    @NonNull
    @Contract("_, _ -> new")
    public static GuitarTuning getGuitarTuningFromTuning(@NonNull Tuning tuning,
                                                         NotesStructure notesStructure) {
        String notesStr = tuning.notes.substring(1, tuning.notes.length() - 1);
        String[] notes = notesStr.split(",");
        assert notes.length != 0 : "Trying to create guitar tuning with 0 notes.";
        return new GuitarTuning(tuning.name, notes, notesStructure);
    }

    public static void deleteAllTunings(@NonNull TuningViewModel tuningViewModel) {
        tuningViewModel.deleteAll();
    }

    public static void updateTuning(@NonNull TuningViewModel tuningViewModel,
                                    String tuningName, Note[] notes) {
        Tuning tuning = new Tuning(tuningName, getNotesStringFromNotesArray(notes));
        tuningViewModel.update(tuning);
    }

    @NonNull
    private static String getNotesStringFromNotesArray(@NonNull Note[] notes) {
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
