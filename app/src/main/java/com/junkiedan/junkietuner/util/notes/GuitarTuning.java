package com.junkiedan.junkietuner.util.notes;

import android.util.Log;
import java.io.Serializable;

/**
 * GuitarTuning class is a different way than Tuning object
 * to hold the information of a Tuning. On this class except for
 * the name of the tuning an array of Note objects is stored
 * for the tuning.
 * @author Stavros Barousis
 */
public class GuitarTuning implements Serializable {
    private final String tuningName;
    private final Note[] notes = new Note[6];

    /**
     * Public constructor that creates a GuitarTuning object.
     * @param tuningName The name of the tuning.
     * @param noteNames The names of the notes of the tuning in
     *                  the formatted note's string. For example
     *                  for Standard E tuning this string should be
     *                  "[E2,A2,D3,G3,B3,E4]".
     */
    public GuitarTuning(String tuningName, String[] noteNames) {
        assert noteNames.length == 6 : "Tried to create a new guitar tuning with != 6 notes.";
        this.tuningName = tuningName;
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

    /**
     * Getter for the Note objects array.
     * @return The array with the Note objects.
     */
    public Note[] getNotes() {
        return notes;
    }

    /**
     * Getter for the Name of the Tuning.
     * @return The String of the Name of the Guitar Tuning.
     */
    public String getTuningName() {
        return tuningName;
    }
}
