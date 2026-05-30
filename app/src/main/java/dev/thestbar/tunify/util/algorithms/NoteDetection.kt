package dev.thestbar.tunify.util.algorithms

import dev.thestbar.tunify.util.notes.Note
import kotlin.math.abs
import kotlin.math.log10

class NoteDetection(private val allNotes: Array<Note>) {

    fun findClosestNote(frequency: Double): Note {
        val len = allNotes.size
        if (frequency <= allNotes[0].frequency) return allNotes[0]
        if (frequency >= allNotes[len - 1].frequency) return allNotes[len - 1]

        var low = 0
        var high = len
        var mid = 0
        while (low < high) {
            mid = (low + high) / 2
            if (allNotes[mid].frequency == frequency) return allNotes[mid]
            if (frequency < allNotes[mid].frequency) {
                if (mid > 0 && frequency > allNotes[mid - 1].frequency) {
                    return compareClosestNoteToTarget(allNotes[mid - 1], allNotes[mid], frequency)
                }
                high = mid
            } else {
                if (mid < len - 1 && frequency < allNotes[mid + 1].frequency) {
                    return compareClosestNoteToTarget(allNotes[mid], allNotes[mid + 1], frequency)
                }
                low = mid + 1
            }
        }
        return allNotes[mid]
    }

    companion object {
        private val LOG2_TO_LOG10_CONVERSION_CONST = 1200.0 * (1 / log10(2.0))

        @JvmStatic
        fun compareClosestNoteToTarget(note1: Note, note2: Note, frequency: Double): Note {
            val delta1 = abs(note1.frequency - frequency)
            val delta2 = abs(note2.frequency - frequency)
            return if (delta1 < delta2) note1 else note2
        }

        @JvmStatic
        fun getDifferentInCents(note: Note?, frequency: Double): Double {
            if (note == null || note.frequency == 0.0) return -1.0
            val delta = frequency / note.frequency
            return LOG2_TO_LOG10_CONVERSION_CONST * log10(delta)
        }
    }
}
