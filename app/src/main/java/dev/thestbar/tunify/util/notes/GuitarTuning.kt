package dev.thestbar.tunify.util.notes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class GuitarTuning(
    val tuningName: String,
    val notes: Array<Note>
) : Parcelable {

    constructor(tuningName: String, noteNames: Array<String>) : this(
        tuningName,
        buildNotes(noteNames)
    )

    companion object {
        private fun buildNotes(noteNames: Array<String>): Array<Note> {
            require(noteNames.size == 6) {
                "GuitarTuning requires exactly 6 notes, got ${noteNames.size}"
            }
            return Array(6) { i ->
                NotesStructure.searchNote(noteNames[i]) ?: run {
                    System.err.println(
                        "GuitarTuning: Search for invalid note - noteName: `${noteNames[i]}` - using A0 instead"
                    )
                    NotesStructure.searchNote("A0")!!
                }
            }
        }
    }
}
