package com.junkiedan.junkietuner.util.notes;

import java.io.Serializable;

public class GuitarTuning implements Serializable {
    private final String tuningName;
    private final Note[] notes = new Note[6];

    public GuitarTuning(String tuningName, String[] noteNames, NotesStructure notesStructure) {
        assert noteNames.length == 6 : "Tried to create a new guitar tuning with != 6 notes.";
        this.tuningName = tuningName;
        Note[] allNotes = notesStructure.getAllNotes();
        int i = 0;
        for (String noteName : noteNames) {
            Note note = notesStructure.searchNote(noteName);
            assert note != null : "Search for invalid note.";
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
