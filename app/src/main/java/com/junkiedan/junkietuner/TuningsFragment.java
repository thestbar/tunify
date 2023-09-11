package com.junkiedan.junkietuner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.junkiedan.junkietuner.core.TuningAdapter;
import com.junkiedan.junkietuner.data.TuningHandler;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TuningsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TuningsFragment extends Fragment {

    private TuningViewModel tuningViewModel;

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

        tuningViewModel = new TuningViewModel(((MainActivity) requireActivity()).getApplication());

        recyclerView = requireView().findViewById(R.id.tuningsList);
        TuningAdapter tuningAdapter = new TuningAdapter(new ArrayList<>(), tuningViewModel);
        recyclerView.setAdapter(tuningAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        final Observer<List<Tuning>> tuningListObserver = tunings -> {
            tuningAdapter.setTuningList(tunings);
            recyclerView.setAdapter(tuningAdapter);
        };
        tuningViewModel.getCurrentTunings().observe(getViewLifecycleOwner(), tuningListObserver);
        addTuningButton = requireView().findViewById(R.id.addTuningButton);
        addTuningButton.setOnClickListener(v -> {
            // Test write to DB
            tuningViewModel.insert(new Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]"));
        });
    }
}