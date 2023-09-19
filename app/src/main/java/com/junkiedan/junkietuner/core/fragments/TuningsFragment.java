package com.junkiedan.junkietuner.core.fragments;

import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.junkiedan.junkietuner.R;
import com.junkiedan.junkietuner.core.PreferencesDataStoreHandler;
import com.junkiedan.junkietuner.core.TuningAdapter;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TuningsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * The fragment that displays all the tunings that are stored in the database.
 * Through this fragment users are able to add new tunings to the database,
 * edit existing ones or delete them.
 * @author Stavros Barousis
 */
public class TuningsFragment extends Fragment {

    // Reference to the recycler view that displays
    // all the tunings from the database.
    private RecyclerView recyclerView;
    // Reference to the FloatingActionButton that
    // is the button that users click in order to
    // add a new tuning to the database.
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
        // Reference to the application object.
        Application application = requireActivity().getApplication();

        // Initialize the reference to the RecyclerView.
        recyclerView = requireView().findViewById(R.id.tuningsList);

        // initSelectedItemId contains the id of the initial selected item.
        // Its value is set to -1.
        int initSelectedItemId = -1;
        try {
            // Inside the try block the CURRENT_TUNING_ID will be retrieved.
            // Here blockingFirst() is used, which blocks the current thread
            // until the value is retrieved.
            // TODO - Change blockingFirst() with asynchronous operation.
            initSelectedItemId = PreferencesDataStoreHandler.getCurrentTuningId(requireContext())
                    .blockingFirst();
        } catch (NullPointerException e) {
            Log.println(Log.WARN, "TuningsFragment@onViewCreated", "When " +
                    "trying to retrieve getCurrentTuningId(context).blockingFirst() a " +
                    "NullPointerException was fired because the value has not been " +
                    "initialized in PreferencesDataStore.");
        }

        // Initialize the TuningAdapter object that will be passed
        // to the RecyclerView.
        TuningAdapter tuningAdapter = new TuningAdapter(new ArrayList<>(),
                getChildFragmentManager(), initSelectedItemId);
        recyclerView.setAdapter(tuningAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // The observer object that observes for changes in the tuning list.
        final Observer<List<Tuning>> tuningListObserver = tunings -> {
            // When a change is observed then the new tunings list is passed
            // to the tuningAdapter.
            tuningAdapter.setTuningList(tunings);
            recyclerView.setAdapter(tuningAdapter);
        };
        // The observe to the current tunings is initialized.
        TuningViewModel.getCurrentTunings(application)
                .observe(getViewLifecycleOwner(), tuningListObserver);

        // Initialize the reference to the addTuningButton and set
        // it's onClickListener to open the AddTuningDialogFragment.
        addTuningButton = requireView().findViewById(R.id.addTuningButton);
        addTuningButton.setOnClickListener(v -> new AddTuningDialogFragment()
                .show(getChildFragmentManager(), "AddTuningDialogFragment"));
    }
}