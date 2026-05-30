package dev.thestbar.tunify.util.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NotesStructureTest {

    @Test
    fun `allNotes contains 96 notes`() {
        assertEquals(96, NotesStructure.allNotes.size)
    }

    @Test
    fun `A4 is the concert pitch 440 Hz`() {
        val a4 = NotesStructure.searchNote("A4")
        assertNotNull(a4)
        assertEquals(440.0, a4!!.frequency, 1e-9)
    }

    @Test
    fun `A0 is at 27_50 Hz`() {
        val a0 = NotesStructure.searchNote("A0")
        assertNotNull(a0)
        assertEquals(27.5, a0!!.frequency, 1e-9)
    }

    @Test
    fun `unknown note name returns null`() {
        assertNull(NotesStructure.searchNote("Z9"))
    }

    @Test
    fun `searchNoteIndex returns -1 for unknown name`() {
        assertEquals(-1, NotesStructure.searchNoteIndex("Z9"))
    }

    @Test
    fun `searchNoteIndex finds A4 at expected position`() {
        val idx = NotesStructure.searchNoteIndex("A4")
        assertEquals(440.0, NotesStructure.allNotes[idx].frequency, 1e-9)
    }

    @Test
    fun `notesAsStringArray length equals allNotes length`() {
        assertEquals(NotesStructure.allNotes.size, NotesStructure.notesAsStringArray.size)
    }

    @Test
    fun `octave 1 below A4 is A3 at 220 Hz`() {
        val a3 = NotesStructure.searchNote("A3")
        assertNotNull(a3)
        assertEquals(220.0, a3!!.frequency, 1e-9)
    }

    @Test
    fun `octave 1 above A4 is A5 at 880 Hz`() {
        val a5 = NotesStructure.searchNote("A5")
        assertNotNull(a5)
        assertEquals(880.0, a5!!.frequency, 1e-9)
    }
}
