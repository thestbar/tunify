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

    }

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
     * TODO Implement Binary Search!
     * @param noteName 0
     * @return 0
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
