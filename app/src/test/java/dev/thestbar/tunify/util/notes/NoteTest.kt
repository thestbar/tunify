package dev.thestbar.tunify.util.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NoteTest {

    @Test
    fun `notes with same name and frequency are equal`() {
        val a = Note("A4", 440.0)
        val b = Note("A4", 440.0)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `notes with different frequencies are not equal`() {
        assertNotEquals(Note("A4", 440.0), Note("A4", 441.0))
    }

    @Test
    fun `toString includes name and frequency`() {
        val s = Note("A4", 440.0).toString()
        assertEquals(true, s.contains("A4"))
        assertEquals(true, s.contains("440.0"))
    }
}
