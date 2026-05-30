package dev.thestbar.tunify.util.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GuitarTuningTest {

    @Test
    fun `valid 6-note construction`() {
        val tuning = GuitarTuning("Standard E", arrayOf("E2", "A2", "D3", "G3", "B3", "E4"))
        assertEquals("Standard E", tuning.tuningName)
        assertEquals(6, tuning.notes.size)
        assertEquals("E2", tuning.notes[0].name)
        assertEquals("E4", tuning.notes[5].name)
    }

    @Test
    fun `unknown note name falls back to A0`() {
        val tuning = GuitarTuning("X", arrayOf("E2", "A2", "Z9", "G3", "B3", "E4"))
        assertEquals("A0", tuning.notes[2].name)
    }

    @Test
    fun `array size not equal to 6 throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            GuitarTuning("X", arrayOf("E2", "A2", "D3"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            GuitarTuning("X", arrayOf("E2", "A2", "D3", "G3", "B3", "E4", "F5"))
        }
    }
}
