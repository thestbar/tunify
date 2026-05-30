package dev.thestbar.tunify.util.notes

import kotlin.math.pow

/**
 * The full note spectrum the tuner can detect: 96 notes covering A0 (27.5 Hz) to G#8 (~6644.88 Hz),
 * centered on concert pitch A4 = 440 Hz.
 */
object NotesStructure {

    private const val CONCERT_PITCH = 440.0
    private const val NOTES_PER_OCTAVE = 12
    private const val OCTAVES = 8
    private val notesAnno = arrayOf(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    )

    val allNotes: Array<Note> by lazy { buildAllNotes() }

    val notesAsStringArray: Array<String> by lazy {
        Array(allNotes.size) { allNotes[it].name }
    }

    fun searchNote(noteName: String): Note? =
        allNotes.firstOrNull { it.name == noteName }

    fun searchNoteIndex(noteName: String): Int =
        allNotes.indexOfFirst { it.name == noteName }

    private fun buildAllNotes(): Array<Note> {
        val total = NOTES_PER_OCTAVE * OCTAVES
        val notes = arrayOfNulls<Note>(total)
        val center = total / 2
        notes[center] = Note("A4", CONCERT_PITCH)
        notes[0] = Note("A0", 27.50)

        var currOctFwd = 4
        var currOctBck = 4
        for (i in 1 until center) {
            val fwd = CONCERT_PITCH * 2.0.pow(i / 12.0)
            val fwdIdx = center + i
            val fwdNoteIdx = (fwdIdx + 9) % 12
            if (fwdNoteIdx == 0) currOctFwd++
            notes[fwdIdx] = Note(notesAnno[fwdNoteIdx] + currOctFwd, fwd)

            val bck = CONCERT_PITCH * 2.0.pow(-i / 12.0)
            val bckIdx = center - i
            val bckNoteIdx = (bckIdx + 9) % 12
            if (bckNoteIdx == 11) currOctBck--
            notes[bckIdx] = Note(notesAnno[bckNoteIdx] + currOctBck, bck)
        }
        @Suppress("UNCHECKED_CAST")
        return notes as Array<Note>
    }
}
