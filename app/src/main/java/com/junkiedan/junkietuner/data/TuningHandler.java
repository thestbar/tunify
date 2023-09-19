package com.junkiedan.junkietuner.data;

import android.app.Application;

import androidx.annotation.NonNull;

import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import com.junkiedan.junkietuner.util.notes.GuitarTuning;
import com.junkiedan.junkietuner.util.notes.Note;

import org.jetbrains.annotations.Contract;

public class TuningHandler {

    @NonNull
    @Contract("_ -> new")
    public static GuitarTuning getGuitarTuningFromTuning(@NonNull Tuning tuning) {
        String notesStr = tuning.notes.substring(1, tuning.notes.length() - 1);
        String[] notes = notesStr.split(",");
        assert notes.length != 0 : "Trying to create guitar tuning with 0 notes.";
        return new GuitarTuning(tuning.name, notes);
    }

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
        // TODO To be continued
    }
}
