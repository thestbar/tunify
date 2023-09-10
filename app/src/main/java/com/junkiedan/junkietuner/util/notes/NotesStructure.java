package com.junkiedan.junkietuner.util.notes;

import java.util.Objects;

/**
 * Class that contains all the notes structure of the spectrum
 * that the algorithm can analyze. On this specific case the algorithm
 * is able to detect frequencies from A0 (27.5 Hz) to G#8 (6644.88).
 * @author Stavros Barousis
 */
public class NotesStructure {

    private Note[] allNotes;
    private final String[] notesAnno = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public NotesStructure() {
        initNotes();
    }

    private void initNotes() {
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
            if (bckNoteIdx == 0) --currOctBck;
            allNotes[bckIdx] = new Note(notesAnno[bckNoteIdx] + currOctBck, bck);
        }
    }

    public Note[] getAllNotes() {
        return allNotes;
    }

    private void printNotes() {
        for (Note note : allNotes) {
            System.out.println(note);
        }
    }

    /**
     * TODO Implement Binary Search!
     * @param noteName 0
     * @return 0
     */
    public Note searchNote(String noteName) {
        for (Note note : allNotes) {
            if (Objects.equals(note.getName(), noteName)) {
                return note;
            }
        }
        return null;
    }

}
