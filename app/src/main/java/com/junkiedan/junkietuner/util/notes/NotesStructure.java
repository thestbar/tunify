package com.junkiedan.junkietuner.util.notes;

import java.util.Objects;

/**
 * Class that contains all the notes structure of the spectrum
 * that the algorithm can analyze. On this specific case the algorithm
 * is able to detect frequencies from A0 (27.5 Hz) to G#8 (6644.88).
 * @author Stavros Barousis
 */
public class NotesStructure {

    private static Note[] allNotes;
    private static String[] stringNoteNames;
    private static final String[] notesAnno = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private NotesStructure() {
        // Private empty constructor. This class will never be instantiated.
    }

    // Function that initializes all the notes.
    private static void initNotes() {
        double concertPitch = 440.0;
        // 12 notes per octave * 8 included octaves for the tuner
        int allNotesLen = 12 * 8;
        allNotes = new Note[allNotesLen];
        allNotes[allNotesLen / 2] = new Note("A4", concertPitch);
        // We need to manually add the 0 index value
        allNotes[0] = new Note("A0", 27.50);
        int i;
        int currOctFwd = 4;
        int currOctBck = 4;
        for (i = 1; i < allNotesLen / 2; ++i) {
            double fwd = concertPitch * Math.pow(2, i / 12.0);
            int fwdIdx = allNotesLen / 2 + i;
            int fwdNoteIdx = (fwdIdx + 9) % 12;
            if (fwdNoteIdx == 0) ++currOctFwd;
            allNotes[fwdIdx] = new Note(notesAnno[fwdNoteIdx] + currOctFwd, fwd);

            double bck = concertPitch * Math.pow(2, -i / 12.0);
            int bckIdx = allNotesLen / 2 - i;
            int bckNoteIdx = (bckIdx + 9) % 12;
            if (bckNoteIdx == 11) --currOctBck;
            allNotes[bckIdx] = new Note(notesAnno[bckNoteIdx] + currOctBck, bck);
        }
    }

    /**
     * Function that returns all the notes in a Note objects array.
     * @return The array of the Note objects.
     */
    public static Note[] getAllNotes() {
        if (allNotes == null) {
            initNotes();
        }
        return allNotes;
    }

    private static void printNotes() {
        for (Note note : allNotes) {
            System.out.println(note);
        }
    }

    /**
     * Searches for a specific note object in the
     * array of the Note objects. The search is performed
     * by the name of the Note.
     * TODO Implement Binary Search!
     * @param noteName The String that contains the Note that
     *                 the function will search of.
     * @return The Note that has the noteName as name or
     * null if the note does not exist.
     */
    public static Note searchNote(String noteName) {
        if (allNotes == null) {
            initNotes();
        }
        for (Note note : allNotes) {
            if (Objects.equals(note.getName(), noteName)) {
                return note;
            }
        }
        return null;
    }

    /**
     * Searches for the index of a specific note object in the
     * array of the Note objects. The search is performed
     * by the name of the Note.
     * TODO Implement Binary Search!
     * @param noteName The String that contains the Note that
     *                 the function will search of.
     * @return The index of the Note that has the noteName as name or
     * -1 if the note does not exist.
     */
    public static int searchNoteIndex(String noteName) {
        if (allNotes == null) {
            initNotes();
        }
        for (int i = 0; i < allNotes.length; ++i) {
            Note note = allNotes[i];
            if (Objects.equals(note.getName(), noteName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Static function that returns all the Notes of the NotesStructure
     * as a big string array.
     * @return The String array that contains all the values of the names
     * of the Notes.
     */
    public static String[] getNotesAsStringArray() {
        if (allNotes == null) {
            initNotes();
        }
        if (stringNoteNames == null) {
            stringNoteNames = new String[allNotes.length];
            int i = 0;
            for (Note note : allNotes) {
                stringNoteNames[i++] = note.getName();
            }
        }
        return stringNoteNames;
    }
}
