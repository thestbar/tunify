package com.junkiedan.junkietuner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.junkiedan.junkietuner.data.TuningHandler;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import com.junkiedan.junkietuner.util.notes.GuitarTuning;
import com.junkiedan.junkietuner.util.notes.Note;
import com.junkiedan.junkietuner.util.notes.NotesStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link DialogFragment} subclass.
 * Use the {@link AddTuningDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddTuningDialogFragment extends DialogFragment {

    private Button okButton;
    private Button cancelButton;
    private TextInputEditText nameInput;
    private List<Spinner> notesList;
    private final Tuning tuning;

    public AddTuningDialogFragment() {
        // Required empty public constructor
        tuning = new Tuning("", "[E2,A2,D3,G3,B3,E4]");
    }

    public AddTuningDialogFragment(Tuning tuning) {
        this.tuning = tuning;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddTuningDialogFragment.
     */
    public static AddTuningDialogFragment newInstance() {
        return new AddTuningDialogFragment();
    }

    public static AddTuningDialogFragment newInstance(Tuning tuning) {
        return new AddTuningDialogFragment(tuning);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notesList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_tuning_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Create references to view components
        okButton = requireView().findViewById(R.id.addTuningOkButton);
        cancelButton = requireView().findViewById(R.id.addTuningCancelButton);
        nameInput = requireView().findViewById(R.id.newTuningNameInput);
        notesList.add(requireView().findViewById(R.id.spinnerNote1));
        notesList.add(requireView().findViewById(R.id.spinnerNote2));
        notesList.add(requireView().findViewById(R.id.spinnerNote3));
        notesList.add(requireView().findViewById(R.id.spinnerNote4));
        notesList.add(requireView().findViewById(R.id.spinnerNote5));
        notesList.add(requireView().findViewById(R.id.spinnerNote6));
        // Create the values for the dropdowns
        ArrayAdapter<CharSequence> arrayAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, NotesStructure.getNotesAsStringArray());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set cancel button to close the dialog
        cancelButton.setOnClickListener(v -> dismiss());
        // Get GuitarTuning from Tuning
        GuitarTuning guitarTuning = TuningHandler.getGuitarTuningFromTuning(tuning);
        // Set initial value for title text input
        nameInput.setText(tuning.name);
        // For each spinner element add the dropdown values
        // and build the initial values
        // If user tries to insert a new row
        // then initialize to Standard E Tuning
        // (E2, B2, G3, D3, B3, E4)
        // If user tries to update a current tuning
        // then initialize the values with
        // current tuning's values
        for (int i = 0; i < notesList.size(); ++i) {
            Spinner currSpinner = notesList.get(i);
            currSpinner.setAdapter(arrayAdapter);
            int idx = NotesStructure.searchNoteIndex(guitarTuning.getNotes()[i].getName());
            currSpinner.setSelection(idx);
        }
        // Add on click listener to OK button
        okButton.setOnClickListener(v -> {
            int len = notesList.size();
            // Update tuning values
            tuning.name = Objects.requireNonNull(nameInput.getText()).toString().trim();
            // Check if name is empty and display error message
            if (tuning.name.equals("")) {
                nameInput.setError("Tuning Name is Required!");
                nameInput.setHint("Enter Tuning Name");
                return;
            }
            // TODO - Do not let the user store the same tunings!
            // Update tuning notes array
            Note[] notes = new Note[len];
            for (int i = 0; i < len; ++i) {
                notes[i] = NotesStructure.searchNote(notesList.get(i).getSelectedItem().toString());
            }
            tuning.notes = TuningHandler.getNotesStringFromNotesArray(notes);
            assert getParentFragment() != null : "Parent fragment is null!";
            // Insert will be able to understand if a current row is being updated
            // because Tuning object will keep its initial id (if it has)
            TuningViewModel.insert(requireActivity().getApplication(), tuning);
            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        // Do nothing (so the window is not closable)
    }
}