package dev.thestbar.tunify.data

import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.util.notes.NotesStructure
import org.junit.Assert.assertEquals
import org.junit.Test

class TuningHandlerTest {

    @Test
    fun `getGuitarTuningFromTuning parses standard E correctly`() {
        val tuning = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]")
        val gt = TuningHandler.getGuitarTuningFromTuning(tuning)
        assertEquals("Standard E", gt.tuningName)
        assertEquals(6, gt.notes.size)
        assertEquals("E2", gt.notes[0].name)
        assertEquals("E4", gt.notes[5].name)
    }

    @Test
    fun `getNotesStringFromNotesArray produces canonical bracketed string`() {
        val notes = arrayOf(
            NotesStructure.searchNote("E2")!!,
            NotesStructure.searchNote("A2")!!,
            NotesStructure.searchNote("D3")!!,
            NotesStructure.searchNote("G3")!!,
            NotesStructure.searchNote("B3")!!,
            NotesStructure.searchNote("E4")!!
        )
        assertEquals("[E2,A2,D3,G3,B3,E4]", TuningHandler.getNotesStringFromNotesArray(notes))
    }

    @Test
    fun `round-trip Tuning - GuitarTuning - String yields original notes string`() {
        val original = Tuning("X", "[E2,A2,D3,G3,B3,E4]")
        val gt = TuningHandler.getGuitarTuningFromTuning(original)
        val roundTripped = TuningHandler.getNotesStringFromNotesArray(gt.notes)
        assertEquals(original.notes, roundTripped)
    }
}
