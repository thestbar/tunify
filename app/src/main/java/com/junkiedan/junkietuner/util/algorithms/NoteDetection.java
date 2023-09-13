package com.junkiedan.junkietuner.util.algorithms;

import androidx.annotation.NonNull;

import com.junkiedan.junkietuner.util.notes.Note;
import com.junkiedan.junkietuner.util.notes.NotesStructure;

/**
 * Class that creates an instance that can search for the closest
 * note of the spectrum of a given frequency. For more details of the
 * notes that the system is able to detect, see the documentation
 * of NotesStructure class.
 * @author Stavros Barousis
 */
public class NoteDetection {
    /**
     * Contains a reference to NotesStructure.allNotes array.
     */
    private final Note[] allNotes;
    private final static double LOG2_TO_LOG10_CONVERSION_CONST = 1200 * (1 / Math.log10(2));

    public NoteDetection() {
        this.allNotes = NotesStructure.getAllNotes();
    }

    /**
     * Given an input frequency in Hertz, the closest note is returned.
     * @param frequency The input frequency on which the algorithm
     *                  will try to find the closest frequency of
     *                  the spectrum that the system is able to detect.
     * @return The closest Note (name, frequency) to the param frequency
     * that was detected.
     */
    public Note findClosestNote(double frequency) {
        int len = allNotes.length;

        // Check corner cases
        if (frequency <= allNotes[0].getFrequency()) {
            return allNotes[0];
        }
        if (frequency >= allNotes[len - 1].getFrequency()) {
            return allNotes[len - 1];
        }

        // Start binary search
        int low = 0;
        int high = len;
        int mid = 0;
        while (low < high) {
            mid = (low + high) / 2;

            if (allNotes[mid].getFrequency() == frequency) {
                return allNotes[mid];
            }

            // If target is less than array element, then search in left
            if (frequency < allNotes[mid].getFrequency()) {
                // If target is greater than previous to mid, return closest of two
                if (mid > 0 && frequency > allNotes[mid - 1].getFrequency()) {
                    return compareClosestNoteToTarget(allNotes[mid - 1], allNotes[mid], frequency);
                }
                // Repeat for left half
                high = mid;
            } else { // If target is greater than mid
                if (mid < len - 1 && frequency < allNotes[mid + 1].getFrequency()) {
                    return compareClosestNoteToTarget(allNotes[mid], allNotes[mid + 1], frequency);
                }
                low = mid + 1;
            }
        }
        return allNotes[mid];
    }

    /**
     * Static method that given two input notes and a frequency,
     * the note to which the frequency is closest to, is returned.
     * @param note1 The first note value that we need to check.
     * @param note2 The second note value that we need to check.
     * @param frequency The frequency for which the system will try to determine
     *                  if note1 or note2 is closest to.
     * @return Reference to one of the variables note1 and note2.
     */
    @NonNull
    public static Note compareClosestNoteToTarget(Note note1, Note note2, double frequency) {
        double delta1 = Math.abs(note1.getFrequency() - frequency);
        double delta2 = Math.abs(note2.getFrequency() - frequency);
        return (delta1 < delta2) ? note1 : note2;
    }

    /**
     * Static method that determines the distance value of a frequency
     * from a note variable's frequency in cents. The unit most commonly
     * used to measure intervals is called cent, from Latin centum,
     * meaning "one hundred". It stands for one hundredth of an equal-tempered
     * semitone. In other words, one octave consists of 1200 cents.
     * Formula for converting the interval frequency ratio f2 / f1 to cents (c or ¢).
     * ¢ or c = 1200 × log2 (f2 / f1). The logarithmic identity logb(a) =
     * log10(a)/log10(b) is commonly used to derive log2 for a number x.
     * Therefore, this helps to create the same formula but using only
     * calculations in log10. c = 1200 × (1 / log10(2)) * log10(f2 / f1).
     * @param note The note around which the distance of frequency from
     *             will be calculated in cents.
     * @param frequency Frequency input for which we want to calculate
     *                  the cent distance from note.
     * @return The distance of frequency from note variable's frequency
     * in cents.
     */
    public static double getDifferentInCents(Note note, double frequency) {
        if (note == null || note.getFrequency() == 0) {
            return -1;
        }
        double delta = frequency / note.getFrequency();
        return LOG2_TO_LOG10_CONVERSION_CONST * Math.log10(delta);
    }
}
