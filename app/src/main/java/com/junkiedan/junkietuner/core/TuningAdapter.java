package com.junkiedan.junkietuner.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.junkiedan.junkietuner.R;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;

import java.util.List;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class TuningAdapter extends RecyclerView.Adapter<TuningAdapter.ViewHolder> {

    // Will hold a reference to the active view model
    private final TuningViewModel tuningViewModel;
    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView notesTextView;
        public ImageButton deleteButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = itemView.findViewById(R.id.tuningName);
            notesTextView = itemView.findViewById(R.id.tuningNotes);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // Store a member variable for the contacts
    private List<Tuning> tuningList;

    // Pass in the contact array into the constructor
    public TuningAdapter(List<Tuning> tuningList, TuningViewModel tuningViewModel) {
        this.tuningList = tuningList;
        this.tuningViewModel = tuningViewModel;
    }

    public void setTuningList(List<Tuning> tuningList) {
        this.tuningList = tuningList;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View tuningView = inflater.inflate(R.layout.tuning_list_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(tuningView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        Tuning tuning = tuningList.get(position);

        // Set item views based on your views and data model
        TextView textViewName = holder.nameTextView;
        textViewName.setText(tuning.name);
        TextView textViewNotes = holder.notesTextView;
        textViewNotes.setText(tuning.notes);
        ImageButton deleteButton = holder.deleteButton;
        deleteButton.setOnClickListener(v -> {
            System.out.println(tuning);
            tuningViewModel.deleteOne(tuning);
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return tuningList.size();
    }

}
