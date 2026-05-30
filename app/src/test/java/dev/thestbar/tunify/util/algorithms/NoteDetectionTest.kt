package dev.thestbar.tunify.util.algorithms

import dev.thestbar.tunify.util.notes.Note
import dev.thestbar.tunify.util.notes.NotesStructure
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteDetectionTest {

    private val nd = NoteDetection(NotesStructure.allNotes)

    @Test
    fun `frequency at or below A0 returns A0`() {
        val a0 = NotesStructure.searchNote("A0")!!
        assertEquals(a0, nd.findClosestNote(10.0))
        assertEquals(a0, nd.findClosestNote(a0.frequency))
    }

    @Test
    fun `frequency at or above last note returns last note`() {
        val last = NotesStructure.allNotes.last()
        assertEquals(last, nd.findClosestNote(20000.0))
        assertEquals(last, nd.findClosestNote(last.frequency))
    }

    @Test
    fun `frequency exactly at A4 returns A4`() {
        val a4 = NotesStructure.searchNote("A4")!!
        assertEquals(a4, nd.findClosestNote(440.0))
    }

    @Test
    fun `frequency just above A4 is still closest to A4`() {
        val a4 = NotesStructure.searchNote("A4")!!
        assertEquals(a4, nd.findClosestNote(441.0))
    }

    @Test
    fun `frequency halfway between A4 and A#4 picks the closer side`() {
        val a4 = NotesStructure.searchNote("A4")!!
        val aSharp4 = NotesStructure.searchNote("A#4")!!
        // A#4 is just over 466 Hz; halfway is ~453 Hz; just below halfway should be A4
        val belowMid = (a4.frequency + aSharp4.frequency) / 2 - 0.5
        assertEquals(a4, nd.findClosestNote(belowMid))
        // Just above halfway should be A#4
        val aboveMid = (a4.frequency + aSharp4.frequency) / 2 + 0.5
        assertEquals(aSharp4, nd.findClosestNote(aboveMid))
    }

    @Test
    fun `cents between same frequency is zero`() {
        val a4 = NotesStructure.searchNote("A4")!!
        assertEquals(0.0, NoteDetection.getDifferentInCents(a4, 440.0), 1e-6)
    }

    @Test
    fun `cents from A4 to A5 is 1200`() {
        val a4 = NotesStructure.searchNote("A4")!!
        assertEquals(1200.0, NoteDetection.getDifferentInCents(a4, 880.0), 1e-6)
    }

    @Test
    fun `cents from A4 to A#4 is 100`() {
        val a4 = NotesStructure.searchNote("A4")!!
        val aSharp4 = NotesStructure.searchNote("A#4")!!
        assertEquals(100.0, NoteDetection.getDifferentInCents(a4, aSharp4.frequency), 1e-6)
    }

    @Test
    fun `cents with zero-frequency note returns -1`() {
        val zero = Note("ZERO", 0.0)
        assertEquals(-1.0, NoteDetection.getDifferentInCents(zero, 100.0), 1e-9)
    }

    @Test
    fun `compareClosestNoteToTarget picks closer note`() {
        val a = Note("A", 100.0)
        val b = Note("B", 200.0)
        assertEquals(a, NoteDetection.compareClosestNoteToTarget(a, b, 110.0))
        assertEquals(b, NoteDetection.compareClosestNoteToTarget(a, b, 190.0))
    }
}
