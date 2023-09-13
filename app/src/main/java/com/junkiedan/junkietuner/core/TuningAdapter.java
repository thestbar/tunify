package com.junkiedan.junkietuner.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.preference.Preference;
import android.util.ArrayMap;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.junkiedan.junkietuner.AddTuningDialogFragment;
import com.junkiedan.junkietuner.MainActivity;
import com.junkiedan.junkietuner.R;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class TuningAdapter extends RecyclerView.Adapter<TuningAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView notesTextView;
        public ImageButton deleteButton;
        public ConstraintLayout constraintLayoutBackground;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = itemView.findViewById(R.id.tuningName);
            notesTextView = itemView.findViewById(R.id.tuningNotes);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            constraintLayoutBackground = itemView.findViewById(R.id.tuningListItemLinearLayoutBackgroundId);
        }

        public void select() {
            constraintLayoutBackground.setBackgroundResource(R.color.custom_taupe_gray);
        }

        public void unselect() {
            constraintLayoutBackground.setBackgroundResource(R.color.custom_raisin_black);
        }
    }

    // Store a member variable for the tunings
    private List<Tuning> tuningList;
    private FragmentManager fragmentManager;
    // Vibrator used for the buzz
    private Vibrator vibrator;
    private Context context;

    private int selectedItemId = -1;
    private static final Map<Integer, ViewHolder> viewHolderMap = new ArrayMap<>();

    // Pass in the contact array into the constructor
    public TuningAdapter(List<Tuning> tuningList, FragmentManager fragmentManager) {
        this.tuningList = tuningList;
        this.fragmentManager = fragmentManager;
    }

    public void setTuningList(List<Tuning> tuningList) {
        this.tuningList = tuningList;
    }
    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        selectedItemId = PreferencesDataStoreHandler.getCurrentTuningId(context)
                .blockingFirst();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Initialize vibrator
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
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
        // Add holder inside viewHolderMap
        viewHolderMap.put(tuning.id, holder);
        if (selectedItemId == tuning.id) {
            holder.select();
        }
        // Set item views based on your views and data model
        holder.nameTextView.setText(tuning.name);
        holder.notesTextView.setText(tuning.notesFormatted());
        holder.deleteButton.setOnClickListener(v -> {
            System.out.println(tuning);
            TuningViewModel.deleteOne(tuning);
        });
        // If it is the selected view then mark it as selected
        if (selectedItemId == tuning.id) {
            holder.select();
        }
        // Set when user long clicks on the whole parent element
        // of the text view to open the edit dialog
        holder.nameTextView.getRootView().setOnLongClickListener(v -> {
            // Play vibration
            vibrator.vibrate(60);
            new AddTuningDialogFragment(tuning)
                    .show(fragmentManager, "EditTuningDialogFragment");
            return true;
        });
        // Set on click the current clicked tuning as the tuning
        // of the main screen
        holder.nameTextView.getRootView().setOnClickListener(v -> {
            vibrator.vibrate(60);
            Log.println(Log.DEBUG, "textViewName.getRootView().setOnClickListener",
                    "Value: " + tuning + " has been selected as default Tuning");
            Objects.requireNonNull(viewHolderMap.get(selectedItemId))
                    .unselect();
            selectedItemId = tuning.id;
            notifyItemChanged(holder.getAdapterPosition());
            holder.select();
            PreferencesDataStoreHandler.setCurrentTuningId(context, tuning.id);
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return tuningList.size();
    }

}
