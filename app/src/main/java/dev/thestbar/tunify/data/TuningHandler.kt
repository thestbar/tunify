package dev.thestbar.tunify.data

import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.util.notes.GuitarTuning
import dev.thestbar.tunify.util.notes.Note

object TuningHandler {

    @JvmStatic
    fun getGuitarTuningFromTuning(tuning: Tuning): GuitarTuning {
        val notesStr = tuning.notes.substring(1, tuning.notes.length - 1)
        val notes = notesStr.split(",").toTypedArray()
        require(notes.isNotEmpty()) { "Trying to create guitar tuning with 0 notes." }
        return GuitarTuning(tuning.name, notes)
    }

    @JvmStatic
    fun getNotesStringFromNotesArray(notes: Array<Note>): String =
        notes.joinToString(separator = ",", prefix = "[", postfix = "]") { it.name }

    /**
     * Wipes the database and re-inserts the bundled defaults.
     * Suspending — invoke from a coroutine scope (e.g., viewModelScope).
     */
    suspend fun resetDatabaseValuesToDefault(repository: TuningRepository) {
        repository.deleteAll()
        DEFAULT_TUNINGS.forEach { (name, notes) ->
            repository.insert(Tuning(name, notes))
        }
    }

    private val DEFAULT_TUNINGS: List<Pair<String, String>> = listOf(
        // Standard tunings
        "Standard E" to "[E2,A2,D3,G3,B3,E4]",
        "Standard Eb/D#" to "[D#2,G#2,C#3,F#3,A#3,D#4]",
        "Standard D" to "[D2,G2,C3,F3,A3,D4]",
        "Standard Db/C#" to "[C#2,F#2,B3,E3,G#3,C#4]",
        "Standard C" to "[C2,F2,A#3,D#3,G3,C4]",
        "Standard F" to "[F2,A#2,D#3,G#3,C3,F4]",
        "Standard G" to "[G2,C3,F3,A#3,D3,G4]",
        // Drop tunings
        "Drop D ('DDD')" to "[D2,A2,D3,G3,B3,E4]",
        "Double Drop D" to "[D2,A2,D3,G3,B3,D4]",
        "Drop C ('Neon')" to "[C2,A2,D3,G3,B3,E4]",
        "Low Drop C" to "[C2,G2,C3,F3,A3,D4]",
        "Drop A ('Slack Thwack')" to "[A1,A2,D3,G3,B3,E4]",
        // Open tunings
        "Open D ('Vestapol')" to "[D2,A2,D3,F#3,A3,D4]",
        "Open Dm ('Bentonia')" to "[D2,A2,D3,F3,A3,D4]",
        "Open Dsus ('DADGAD')" to "[D2,A2,D3,G3,A3,D4]",
        "Open G ('Taro Patch')" to "[D2,G2,D3,G3,B3,D4]",
        "Open Gm ('Banjo Minor')" to "[D2,G2,D3,G3,A#3,D4]",
        "Open Gsus ('Sawmill')" to "[D2,G2,D3,G3,C4,D4]",
        "Open C ('Wide Major')" to "[C2,G2,C3,G3,C4,E4]",
        "Open Cm ('Wide Minor')" to "[C2,G2,C3,G3,C4,D#4]",
        "Open Csus ('Wide Modal')" to "[C2,G2,C3,G3,C4,F4]",
        "Open E ('Vestapol')" to "[E2,B2,E3,G#3,B4,E4]",
        "Open Em ('Cross note')" to "[E2,B2,E3,G3,B4,E4]",
        "Open F ('Low Taro')" to "[C2,F2,C3,F3,A3,C4]",
        "Open Fm ('Low Banjo')" to "[C2,F2,C3,F3,A3,C4]",
        "Open A ('Spanish')" to "[E2,A2,E3,A3,C#4,E4]",
        // Interval tunings
        "All Minor 3rds ('Diminished')" to "[G2,A#2,C#3,E3,G4,A#4]",
        "All Minor 3rds ('Augmented')" to "[E2,G#2,C3,E3,G#3,C4]",
        "All Perfect 4ths ('Regular')" to "[E2,A2,D3,G3,C4,F4]",
        "All Tritones ('Symmetric')" to "[C2,F#2,C3,F#3,C4,F#4]",
        "All Perfect 5ths ('Quintal')" to "[A2,E3,B3,F#4,C#5,G#5]",
        "All Minor 6ths ('Aug. Flip')" to "[F2,C#3,A3,F4,C#5,A5]",
        // Global tunings
        "Ali Farka Touré" to "[G2,A2,D3,G3,B3,E4]",
        "Atta's C" to "[C2,G2,E3,G3,C4,E4]",
        "Bağlama/Saz" to "[G2,G3,D3,D4,A3,A4]",
        "Carnatic ('Drake's Drone')" to "[B2,E3,B3,E4,B4,E5]",
        "Charango" to "[G2,G3,C3,E3,A3,E4]",
        "Haja's Bb" to "[A#2,F3,C3,F3,A#3,C4]",
        "Jack's Chikari" to "[D2,D2,D3,G3,B3,E4]",
        "Kabosy" to "[C2,G2,D3,G3,B3,D4]",
        "Mauna Loa C6" to "[C2,G2,C3,G3,A3,E4]",
        "Mi-composé ('Elenga')" to "[E2,A2,D4,G3,B3,E4]",
        "Orkney" to "[C2,G2,D3,G3,C4,D4]",
        "Oud (Arabic)" to "[E2,A2,C#3,F#3,B3,E4]",
        "Oud (Turkish)" to "[E2,A2,B2,E3,A3,D4]",
        "Papuan Four-Key" to "[F2,A#2,C3,F3,A3,C4]",
        "Rakotomavo" to "[A#2,F3,C3,G3,C4,E4]",
        "Keola's C ('Wahine')" to "[C2,G2,D3,G3,B3,E4]",
        "Zen Drone ('Dulcimeric')" to "[D2,A2,D3,A3,A3,D4]",
        // Artist / track tunings
        "AirTap" to "[F2,A2,C3,F3,C4,F4]",
        "Albert Collins Fm" to "[F2,C3,F3,G#3,C4,F4]",
        "Albert King F6" to "[C2,F2,C3,F3,A3,D4]",
        "Black Crow" to "[A#2,A#3,C#4,F4,A4,A#4]",
        "Blown a Wish" to "[F2,C3,F3,A#3,A#3,G4]",
        "Bruce Palmer ('Judy Blue Eyes')" to "[E2,E2,E3,E3,B3,E4]",
        "Cello ('Haircut')" to "[C2,G2,D3,A3,B3,E4]",
        "Coyote" to "[C2,G2,D3,F3,C4,E4]",
        "Dracula" to "[C2,G2,C3,F3,A#3,D4]",
        "Equilibrium" to "[G1,A2,D3,E3,A3,E4]",
        "Ethereal" to "[D2,A2,C#3,F#3,C#4,D4]",
        "Fripp's New Standard ('Crafty')" to "[C2,G2,D3,A3,E4,G4]",
        "Funky Avocado" to "[B1,A2,D3,G3,A3,D4]",
        "Gambale" to "[A2,D3,G3,C4,E4,A4]",
        "Ghost Reveries" to "[D2,A2,D3,F3,A3,E4]",
        "Godzilla" to "[C#2,G#2,D#3,E3,B3,E4]",
        "Gothic" to "[D#2,G2,D3,A3,A#3,C4]",
        "Hejira" to "[C2,G2,D3,F3,G3,C4]",
        "I Only Said" to "[E2,A2,B2,G3,G3,E4]",
        "Iris" to "[B1,D2,D3,D3,D4,D4]",
        "José González & Jigsaw Falling Into Place" to "[D2,A2,D3,F#3,B3,E4]",
        "Karnivool" to "[B1,F#2,B2,G3,B3,E4]",
        "Magic Farmer" to "[C2,F2,C3,G3,A3,E4]",
        "Road" to "[E2,A2,D3,E3,B3,E4]",
        "Only Shallow" to "[E2,B2,E3,F#3,B3,E4]",
        "One-Tone Drone ('Ostrich')" to "[D2,D3,D3,D3,D4,D4]",
        "Pink Moon" to "[C2,G2,C3,F3,C4,E4]",
        "Place to Be" to "[C2,G2,C3,F3,G3,E4]",
        "Schizophrenia" to "[F#2,F#2,G3,G3,A3,A3]",
        "Teardrop" to "[D2,A2,D3,A3,B3,E4]",
        "Wind of Change" to "[D2,D2,D3,A3,D4,F#4]",
        "Yvette's Dadd9" to "[D2,A2,D3,F#3,A3,E4]",
        // Miscellaneous tunings
        "Alphabet" to "[A2,B3,C4,D4,E4,F4]",
        "Banjo/Overtones" to "[G2,G2,D3,G3,B3,D4]",
        "Cabbage" to "[C2,A2,A#2,A3,G4,E5]",
        "Drop DG" to "[D2,G2,D3,G3,B3,E4]",
        "Fuji" to "[A#1,G2,D3,G3,G3,D4]",
        "Icarus" to "[D2,A2,D3,G3,B3,C4]",
        "Mesopotamian" to "[B1,A2,G3,D4,A4,D5]",
        "Lefty Flip ('Mirrored')" to "[E2,B2,G2,D3,A3,E4]",
        "Lute/Vihuela" to "[E2,A2,D3,F#3,B3,E4]",
        "Math Rock F" to "[F2,A2,C3,G3,C4,E4]",
        "Nashville" to "[E3,A3,D4,G4,B3,E4]",
        "Overtone Series" to "[G2,B2,D3,F3,G3,A3]",
        "Papa-Papa" to "[D2,A2,D3,D3,A3,D4]",
        "Ten Years" to "[D2,E2,C3,A3,D4,E4]",
        "Ead-Gad" to "[E2,A2,D3,G3,A3,D4]",
        "Zigzag 3rds (Minor)" to "[F2,G#2,C3,D#3,G3,A#3]",
        "Zigzag 3rds (Major)" to "[F2,A2,C3,E3,G3,B3]"
    )
}
