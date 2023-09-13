package com.junkiedan.junkietuner;

import static android.content.DialogInterface.BUTTON_POSITIVE;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.junkiedan.junkietuner.core.TuningAdapter;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import com.junkiedan.junkietuner.util.notes.NotesStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TuningsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TuningsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton addTuningButton;

    public TuningsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TuningsFragment.
     */
    public static TuningsFragment newInstance() {
        return new TuningsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tunings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Application application = ((MainActivity) requireActivity()).getApplication();

        recyclerView = requireView().findViewById(R.id.tuningsList);
        TuningAdapter tuningAdapter = new TuningAdapter(new ArrayList<>(),
                getChildFragmentManager());
        recyclerView.setAdapter(tuningAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        final Observer<List<Tuning>> tuningListObserver = tunings -> {
            tuningAdapter.setTuningList(tunings);
            recyclerView.setAdapter(tuningAdapter);
        };
        TuningViewModel.getCurrentTunings(application)
                .observe(getViewLifecycleOwner(), tuningListObserver);
        addTuningButton = requireView().findViewById(R.id.addTuningButton);
        addTuningButton.setOnClickListener(v -> {
            new AddTuningDialogFragment()
                    .show(getChildFragmentManager(), "AddTuningDialogFragment");
        });
    }
}