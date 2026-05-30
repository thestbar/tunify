package dev.thestbar.tunify.data.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class TuningTest {

    @Test
    fun `notesFormatted converts bracketed comma string to double-space separated`() {
        val tuning = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]")
        assertEquals("E2  A2  D3  G3  B3  E4", tuning.notesFormatted())
    }

    @Test
    fun `notesFormatted handles sharps`() {
        val tuning = Tuning("Standard Eb", "[D#2,G#2,C#3,F#3,A#3,D#4]")
        assertEquals("D#2  G#2  C#3  F#3  A#3  D#4", tuning.notesFormatted())
    }

    @Test
    fun `notesFormatted handles single-note tuning`() {
        val tuning = Tuning("One", "[A4]")
        assertEquals("A4", tuning.notesFormatted())
    }

    @Test
    fun `notesFormatted handles empty brackets`() {
        val tuning = Tuning("Empty", "[]")
        assertEquals("", tuning.notesFormatted())
    }

    @Test
    fun `equality is based on all fields including id`() {
        val a = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]").apply { id = 1 }
        val b = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]").apply { id = 1 }
        val c = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]").apply { id = 2 }
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assert(a != c)
    }
}
