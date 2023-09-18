package com.junkiedan.junkietuner.util.notes;

import android.os.Debug;
import android.util.Log;

import java.io.Serializable;

public class GuitarTuning implements Serializable {
    private final String tuningName;
    private final Note[] notes = new Note[6];

    public GuitarTuning(String tuningName, String[] noteNames) {
        assert noteNames.length == 6 : "Tried to create a new guitar tuning with != 6 notes.";
        this.tuningName = tuningName;
        Note[] allNotes = NotesStructure.getAllNotes();
        int i = 0;
        for (String noteName : noteNames) {
            Note note = NotesStructure.searchNote(noteName);
            if (note == null) {
                Log.println(Log.ERROR, "GuitarTuning@Constructor",
                        "Search for invalid note - noteName: `" + noteName +
                                "` - Select A0 instead of this note");
                note = NotesStructure.searchNote("A0");
            }
            notes[i++] = note;
        }
    }

    public Note[] getNotes() {
        return notes;
    }

    public String getTuningName() {
        return tuningName;
    }
}
