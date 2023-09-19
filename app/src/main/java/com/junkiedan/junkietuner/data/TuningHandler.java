package com.junkiedan.junkietuner.data;

import android.app.Application;
import androidx.annotation.NonNull;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import com.junkiedan.junkietuner.util.notes.GuitarTuning;
import com.junkiedan.junkietuner.util.notes.Note;
import org.jetbrains.annotations.Contract;

/**
 * This class contains some static methods that are used to manipulate
 * the Guitar Tunings in order to perform CRUD operations to the database
 * and display information of the screen of the application.
 * @author Stavros Barousis
 */
public class TuningHandler {

    /**
     * Creates a GuitarTuning object from a Tuning object. The base
     * difference between a GuitarTuning and a Tuning object is that
     * the former contains an array of Note class objects that can be
     * used to perform some calculation when the actual Note objects
     * are needed and the latter contains the notes in a String format
     * (For example "[E2,A2,D3,G3,B3,E4]".
     * @param tuning The tuning object that will be used to create
     *               the GuitarTuning object.
     * @return The GuitarTuningObject that was created from the
     * Tuning object.
     */
    @NonNull
    @Contract("_ -> new")
    public static GuitarTuning getGuitarTuningFromTuning(@NonNull Tuning tuning) {
        String notesStr = tuning.notes.substring(1, tuning.notes.length() - 1);
        String[] notes = notesStr.split(",");
        assert notes.length != 0 : "Trying to create guitar tuning with 0 notes.";
        return new GuitarTuning(tuning.name, notes);
    }

    /**
     * Given a Note object the formatted tuning notes list is constructed.
     * For example for the notes that are stored in a note array E2 A2 D3 G3 B3 E4
     * the string "[E2,A2,D3,G3,B3,E4]" is constructed.
     * @param notes The Note objects array that will be used to create the String.
     * @return The Note Array String formatted as explained above.
     */
    @NonNull
    public static String getNotesStringFromNotesArray(@NonNull Note[] notes) {
        StringBuilder notesStr = new StringBuilder();
        notesStr.append("[");
        for (Note note : notes) {
            notesStr.append(note.getName()).append(",");
        }
        notesStr.deleteCharAt(notesStr.length() - 1);
        notesStr.append("]");
        return notesStr.toString();
    }

    /**
     * Function that initializes the database with the standard tunings.
     * @param application The Application Object.
     */
    public static void resetDatabaseValuesToDefault(Application application) {
        // Delete everything (if applicable)
        TuningViewModel.deleteAll(application);
        // Standard Tunings
        // Standard E
        // Thanks https://ragajunglism.org/tunings/menu/ for the tunings
        Tuning tuning = new Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Standard Eb
        tuning = new Tuning("Standard Eb/D#", "[D#2,G#2,C#3,F#3,A#3,D#4]");
        TuningViewModel.insert(application, tuning);
        // Standard D
        tuning = new Tuning("Standard D", "[D2,G2,C3,F3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Standard Db
        tuning = new Tuning("Standard Db/C#", "[C#2,F#2,B3,E3,G#3,C#4]");
        TuningViewModel.insert(application, tuning);
        // Standard C
        tuning = new Tuning("Standard C", "[C2,F2,A#3,D#3,G3,C4]");
        TuningViewModel.insert(application, tuning);
        // Standard F
        tuning = new Tuning("Standard F", "[F2,A#2,D#3,G#3,C3,F4]");
        TuningViewModel.insert(application, tuning);
        // Standard G
        tuning = new Tuning("Standard G", "[G2,C3,F3,A#3,D3,G4]");
        TuningViewModel.insert(application, tuning);
        // Drop Tunings
        // Drop D ('DDD')
        tuning = new Tuning("Drop D ('DDD')", "[D2,A2,D3,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Double Drop D
        tuning = new Tuning("Double Drop D", "[D2,A2,D3,G3,B3,D4]");
        TuningViewModel.insert(application, tuning);
        // Drop C ('Neon')
        tuning = new Tuning("Drop C ('Neon')", "[C2,A2,D3,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Low Drop C
        tuning = new Tuning("Low Drop C", "[C2,G2,C3,F3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Drop A ('Slack Thwack')
        tuning = new Tuning("Drop A ('Slack Thwack')", "[A1,A2,D3,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Open Tunings
        // Open D ('Vestapol')
        tuning = new Tuning("Open D ('Vestapol')", "[D2,A2,D3,F#3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Open Dm ('Bentonia')
        tuning = new Tuning("Open Dm ('Bentonia')", "[D2,A2,D3,F3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Open Dsus ('DADGAD')
        tuning = new Tuning("Open Dsus ('DADGAD')", "[D2,A2,D3,G3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Open G ('Taro Patch')
        tuning = new Tuning("Open G ('Taro Patch')", "[D2,G2,D3,G3,B3,D4]");
        TuningViewModel.insert(application, tuning);
        // Open Gm ('Banjo Minor')
        tuning = new Tuning("Open Gm ('Banjo Minor')", "[D2,G2,D3,G3,A#3,D4]");
        TuningViewModel.insert(application, tuning);
        // Open Gsus ('Sawmill')
        tuning = new Tuning("Open Gsus ('Sawmill')", "[D2,G2,D3,G3,C4,D4]");
        TuningViewModel.insert(application, tuning);
        // Open C ('Wide Major')
        tuning = new Tuning("Open C ('Wide Major')", "[C2,G2,C3,G3,C4,E4]");
        TuningViewModel.insert(application, tuning);
        // Open Cm ('Wide Minor')
        tuning = new Tuning("Open Cm ('Wide Minor')", "[C2,G2,C3,G3,C4,D#4]");
        TuningViewModel.insert(application, tuning);
        // Open Csus ('Wide Modal')
        tuning = new Tuning("Open Csus ('Wide Modal')", "[C2,G2,C3,G3,C4,F4]");
        TuningViewModel.insert(application, tuning);
        // Open E ('Vestapol')
        tuning = new Tuning("Open E ('Vestapol')", "[E2,B2,E3,G#3,B4,E4]");
        TuningViewModel.insert(application, tuning);
        // Open Em ('Cross-note')
        tuning = new Tuning("Open Em ('Cross note')", "[E2,B2,E3,G3,B4,E4]");
        TuningViewModel.insert(application, tuning);
        // Open F ('Low Taro')
        tuning = new Tuning("Open F ('Low Taro')", "[C2,F2,C3,F3,A3,C4]");
        TuningViewModel.insert(application, tuning);
        // Open Fm ('Low Banjo')
        tuning = new Tuning("Open Fm ('Low Banjo')", "[C2,F2,C3,F3,A3,C4]");
        TuningViewModel.insert(application, tuning);
        // Open A ('Spanish')
        tuning = new Tuning("Open A ('Spanish')", "[E2,A2,E3,A3,C#4,E4]");
        TuningViewModel.insert(application, tuning);
        // Interval Tunings
        // All Minor 3rds ('Diminished')
        tuning = new Tuning("All Minor 3rds ('Diminished')", "[G2,A#2,C#3,E3,G4,A#4]");
        TuningViewModel.insert(application, tuning);
        // All Major 3rds ('Augmented')
        tuning = new Tuning("All Minor 3rds ('Augmented')", "[E2,G#2,C3,E3,G#3,C4]");
        TuningViewModel.insert(application, tuning);
        // All Perfect 4ths ('Regular')
        tuning = new Tuning("All Perfect 4ths ('Regular')", "[E2,A2,D3,G3,C4,F4]");
        TuningViewModel.insert(application, tuning);
        // All Tritones ('Symmetric')
        tuning = new Tuning("All Tritones ('Symmetric')", "[C2,F#2,C3,F#3,C4,F#4]");
        TuningViewModel.insert(application, tuning);
        // All Perfect 5ths ('Quintal')
        tuning = new Tuning("All Perfect 5ths ('Quintal')", "[A2,E3,B3,F#4,C#5,G#5]");
        TuningViewModel.insert(application, tuning);
        // All Minor 6ths ('Aug. Flip')
        tuning = new Tuning("All Minor 6ths ('Aug. Flip')", "[F2,C#3,A3,F4,C#5,A5]");
        TuningViewModel.insert(application, tuning);
        // Global Tunings
        // Ali Farka Toure
        tuning = new Tuning("Ali Farka Touré", "[G2,A2,D3,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Atta's C
        tuning = new Tuning("Atta's C", "[C2,G2,E3,G3,C4,E4]");
        TuningViewModel.insert(application, tuning);
        // Bağlama/Saz
        tuning = new Tuning("Bağlama/Saz", "[G2,G3,D3,D4,A3,A4]");
        TuningViewModel.insert(application, tuning);
        // Carnatic ('Drake’s Drone')
        tuning = new Tuning("Carnatic ('Drake’s Drone')", "[B2,E3,B3,E4,B4,E5]");
        TuningViewModel.insert(application, tuning);
        // Charango
        tuning = new Tuning("Charango", "[G2,G3,C3,E3,A3,E4]");
        TuningViewModel.insert(application, tuning);
        // Haja's Bb
        tuning = new Tuning("Haja's Bb", "[A#2,F3,C3,F3,A#3,C4]");
        TuningViewModel.insert(application, tuning);
        // Jack's Chikari
        tuning = new Tuning("Jack's Chikari", "[D2,D2,D3,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Kabosy
        tuning = new Tuning("Kabosy", "[C2,G2,D3,G3,B3,D4]");
        TuningViewModel.insert(application, tuning);
        // Mauna Loa C6
        tuning = new Tuning("Mauna Loa C6", "[C2,G2,C3,G3,A3,E4]");
        TuningViewModel.insert(application, tuning);
        // Mi-composé ('Elenga')
        tuning = new Tuning("Mi-composé ('Elenga')", "[E2,A2,D4,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Orkney
        tuning = new Tuning("Orkney", "[C2,G2,D3,G3,C4,D4]");
        TuningViewModel.insert(application, tuning);
        // Oud (Arabic)
        tuning = new Tuning("Oud (Arabic)", "[E2,A2,C#3,F#3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Oud (Turkish)
        tuning = new Tuning("Oud (Turkish)", "[E2,A2,B2,E3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Papuan Four-Key
        tuning = new Tuning("Papuan Four-Key", "[F2,A#2,C3,F3,A3,C4]");
        TuningViewModel.insert(application, tuning);
        // Rakotomavo
        tuning = new Tuning("Rakotomavo", "[A#2,F3,C3,G3,C4,E4]");
        TuningViewModel.insert(application, tuning);
        // Keola's C ('Wahine')
        tuning = new Tuning("Keola's C ('Wahine')", "[C2,G2,D3,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Zen Drone ('Dulcimeric')
        tuning = new Tuning("Zen Drone ('Dulcimeric')", "[D2,A2,D3,A3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Artist/Track Tunings
        // AirTap
        tuning = new Tuning("AirTap", "[F2,A2,C3,F3,C4,F4]");
        TuningViewModel.insert(application, tuning);
        // Albert Collins Fm
        tuning = new Tuning("Albert Collins Fm", "[F2,C3,F3,G#3,C4,F4]");
        TuningViewModel.insert(application, tuning);
        // Albert King F6
        tuning = new Tuning("Albert King F6", "[C2,F2,C3,F3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Black Crow
        tuning = new Tuning("Black Crow", "[A#2,A#3,C#4,F4,A4,A#4]");
        TuningViewModel.insert(application, tuning);
        // Blown a Wish
        tuning = new Tuning("Blown a Wish", "[F2,C3,F3,A#3,A#3,G4]");
        TuningViewModel.insert(application, tuning);
        // Bruce Palmer ('Judy Blue Eyes')
        tuning = new Tuning("Bruce Palmer ('Judy Blue Eyes')", "[E2,E2,E3,E3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Cello ('Haircut')
        tuning = new Tuning("Cello ('Haircut')", "[C2,G2,D3,A3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Coyote
        tuning = new Tuning("Coyote", "[C2,G2,D3,F3,C4,E4]");
        TuningViewModel.insert(application, tuning);
        // Dracula
        tuning = new Tuning("Dracula", "[C2,G2,C3,F3,A#3,D4]");
        TuningViewModel.insert(application, tuning);
        // Equilibrium
        tuning = new Tuning("Equilibrium", "[G1,A2,D3,E3,A3,E4]");
        TuningViewModel.insert(application, tuning);
        // Ethereal
        tuning = new Tuning("Ethereal", "[D2,A2,C#3,F#3,C#4,D4]");
        TuningViewModel.insert(application, tuning);
        // Fripp's New Standard ('Crafty')
        tuning = new Tuning("Fripp's New Standard ('Crafty')", "[C2,G2,D3,A3,E4,G4]");
        TuningViewModel.insert(application, tuning);
        // Funky Avocado
        tuning = new Tuning("Funky Avocado", "[B1,A2,D3,G3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Gambale
        tuning = new Tuning("Gambale", "[A2,D3,G3,C4,E4,A4]");
        TuningViewModel.insert(application, tuning);
        // Ghost Reveries
        tuning = new Tuning("Ghost Reveries", "[D2,A2,D3,F3,A3,E4]");
        TuningViewModel.insert(application, tuning);
        // Godzilla
        tuning = new Tuning("Godzilla", "[C#2,G#2,D#3,E3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Gothic
        tuning = new Tuning("Gothic", "[D#2,G2,D3,A3,A#3,C4]");
        TuningViewModel.insert(application, tuning);
        // Hejira
        tuning = new Tuning("Hejira", "[C2,G2,D3,F3,G3,C4]");
        TuningViewModel.insert(application, tuning);
        // I Only Said
        tuning = new Tuning("I Only Said", "[E2,A2,B2,G3,G3,E4]");
        TuningViewModel.insert(application, tuning);
        // Iris
        tuning = new Tuning("Iris", "[B1,D2,D3,D3,D4,D4]");
        TuningViewModel.insert(application, tuning);
        // José González
        tuning = new Tuning("José González & Jigsaw Falling Into Place",
                "[D2,A2,D3,F#3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Karnivool
        tuning = new Tuning("Karnivool", "[B1,F#2,B2,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Magic Farmer
        tuning = new Tuning("Magic Farmer", "[C2,F2,C3,G3,A3,E4]");
        TuningViewModel.insert(application, tuning);
        // Road
        tuning = new Tuning("Road", "[E2,A2,D3,E3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Only Shallow
        tuning = new Tuning("Only Shallow", "[E2,B2,E3,F#3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // One-Tone Drone ('Ostrich')
        tuning = new Tuning("One-Tone Drone ('Ostrich')", "[D2,D3,D3,D3,D4,D4]");
        TuningViewModel.insert(application, tuning);
        // Pink Moon
        tuning = new Tuning("Pink Moon", "[C2,G2,C3,F3,C4,E4]");
        TuningViewModel.insert(application, tuning);
        // Place to Be
        tuning = new Tuning("Place to Be", "[C2,G2,C3,F3,G3,E4]");
        TuningViewModel.insert(application, tuning);
        // Schizophrenia
        tuning = new Tuning("Schizophrenia", "[F#2,F#2,G3,G3,A3,A3]");
        TuningViewModel.insert(application, tuning);
        // Teardrop
        tuning = new Tuning("Teardrop", "[D2,A2,D3,A3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Wind of Change
        tuning = new Tuning("Wind of Change", "[D2,D2,D3,A3,D4,F#4]");
        TuningViewModel.insert(application, tuning);
        // Yvette's Dadd9
        tuning = new Tuning("Yvette's Dadd9", "[D2,A2,D3,F#3,A3,E4]");
        TuningViewModel.insert(application, tuning);
        // Miscellaneous TUnings
        // Alphabet
        tuning = new Tuning("Alphabet", "[A2,B3,C4,D4,E4,F4]");
        TuningViewModel.insert(application, tuning);
        // Banjo/Overtones
        tuning = new Tuning("Banjo/Overtones", "[G2,G2,D3,G3,B3,D4]");
        TuningViewModel.insert(application, tuning);
        // Cabbage
        tuning = new Tuning("Cabbage", "[C2,A2,A#2,A3,G4,E5]");
        TuningViewModel.insert(application, tuning);
        // Drop DG
        tuning = new Tuning("Drop DG", "[D2,G2,D3,G3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Fuji
        tuning = new Tuning("Fuji", "[A#1,G2,D3,G3,G3,D4]");
        TuningViewModel.insert(application, tuning);
        // Icarus
        tuning = new Tuning("Icarus", "[D2,A2,D3,G3,B3,C4]");
        TuningViewModel.insert(application, tuning);
        // Mesopotamian
        tuning = new Tuning("Mesopotamian", "[B1,A2,G3,D4,A4,D5]");
        TuningViewModel.insert(application, tuning);
        // Lefty Flip ('Mirrored')
        tuning = new Tuning("Lefty Flip ('Mirrored')", "[E2,B2,G2,D3,A3,E4]");
        TuningViewModel.insert(application, tuning);
        // Lute/Vihuela
        tuning = new Tuning("Lute/Vihuela", "[E2,A2,D3,F#3,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Math Rock F
        tuning = new Tuning("Math Rock F", "[F2,A2,C3,G3,C4,E4]");
        TuningViewModel.insert(application, tuning);
        // Nashville
        tuning = new Tuning("Nashville", "[E3,A3,D4,G4,B3,E4]");
        TuningViewModel.insert(application, tuning);
        // Overtone Series
        tuning = new Tuning("Overtone Series", "[G2,B2,D3,F3,G3,A3]");
        TuningViewModel.insert(application, tuning);
        // Papa-Papa
        tuning = new Tuning("Papa-Papa", "[D2,A2,D3,D3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Ten Years
        tuning = new Tuning("Ten Years", "[D2,E2,C3,A3,D4,E4]");
        TuningViewModel.insert(application, tuning);
        // Ead-Gad
        tuning = new Tuning("Ead-Gad", "[E2,A2,D3,G3,A3,D4]");
        TuningViewModel.insert(application, tuning);
        // Zigzag 3rds (Minor)
        tuning = new Tuning("Zigzag 3rds (Minor)", "[F2,G#2,C3,D#3,G3,A#3]");
        TuningViewModel.insert(application, tuning);
        // Zigzag 3rds (Major)
        tuning = new Tuning("Zigzag 3rds (Major)", "[F2,A2,C3,E3,G3,B3]");
        TuningViewModel.insert(application, tuning);
    }
}
