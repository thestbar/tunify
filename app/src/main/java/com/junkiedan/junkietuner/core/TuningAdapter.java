package com.junkiedan.junkietuner.core;

import android.app.Application;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.junkiedan.junkietuner.R;
import com.junkiedan.junkietuner.core.fragments.AddTuningDialogFragment;
import com.junkiedan.junkietuner.data.entities.Tuning;
import com.junkiedan.junkietuner.data.viewmodels.TuningViewModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Create the basic adapter extending from RecyclerView.Adapter.
 * Note that we specify the custom ViewHolder which gives us access to our views.
 * @author Stavros Barousis
 */
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
    // Reference to the fragment manager that is used
    // to open the new tuning dialog fragment
    private final FragmentManager fragmentManager;
    // Vibrator used for the buzz
    private Vibrator vibrator;
    // Reference to the parent context (might be unnecessary)
    private Context context;
    // Holds the id (database tuning.id) of the currently
    // selected tuning
    private int selectedItemId;
    // Map that holds a reference on every view holder that is active
    // TODO - viewHolderMap never deletes its values even if a row is completely
    //        deleted from the database. Also, counting the fact that it is static
    //        this might lead to some extra memory usage that is unnecessary.
    //        Planning to delete items from this HashMap and also check if there are
    //        any ways that can help probably improve performance and reduce the risk
    //        of extra memory usage.
    private static final Map<Integer, ViewHolder> viewHolderMap = new HashMap<>();

    /**
     * Public constructor that creates a new Tuning Adapter that will be
     * used by the recycler view item that displays all the tuning of
     * the application.
     * @param tuningList Pass in the tuning list into the constructor.
     * @param fragmentManager Reference to the fragment manager.
     * @param selectedItemId Currently selected item id (Value is fetched
     *                       from Preferences DataStore).
     */
    public TuningAdapter(List<Tuning> tuningList, FragmentManager fragmentManager,
                         int selectedItemId) {
        this.tuningList = tuningList;
        this.fragmentManager = fragmentManager;
        this.selectedItemId = selectedItemId;
    }

    /**
     * Setter method for the tunings list.
     * @param tuningList The changed tuning list that needs to be displayed
     *                   from the adapter.
     */
    public void setTuningList(List<Tuning> tuningList) {
        this.tuningList = tuningList;
    }

    /**
     * This method usually involves inflating a layout from XML
     * and returning the holder.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return The currently created view holder item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        // Initialize vibrator
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Inflate the custom layout
        View tuningView = inflater.inflate(R.layout.tuning_list_item, parent, false);
        // Return a new holder instance
        return new ViewHolder(tuningView);
    }

    /**
     * Involves populating data into the item through holder
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        Tuning tuning = tuningList.get(holder.getLayoutPosition());
        // Add holder inside viewHolderMap
        viewHolderMap.put(tuning.id, holder);
        if (selectedItemId == tuning.id) {
            holder.select();
        } else {
            holder.unselect();
        }
        // Set item views based on your views and data model
        holder.nameTextView.setText(tuning.name);
        holder.notesTextView.setText(tuning.notesFormatted());
        holder.deleteButton.setOnClickListener(v -> {
            Log.println(Log.DEBUG, "Deleting Tuning From DB", String.valueOf(tuning));
            TuningViewModel.deleteOne((Application) context.getApplicationContext(), tuning);
        });
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
            try {
                Objects.requireNonNull(viewHolderMap.get(selectedItemId))
                        .unselect();
            } catch (NullPointerException e) {
                Log.println(Log.WARN, "TuningAdapter@onBindBiewHolder@setOnClickListener@" +
                        "holder.nameTextView.getRootView()", "NullPointerException was " +
                        "fired when trying to unselect current item, because there is no " +
                        "previously selected item id = `" + selectedItemId + "`");
            }
            selectedItemId = tuning.id;
            notifyItemChanged(holder.getLayoutPosition());
            holder.select();
            PreferencesDataStoreHandler.setCurrentTuningId(context, tuning.id);
        });
    }

    /**
     * @return Total count of items in the list
     */
    @Override
    public int getItemCount() {
        return tuningList.size();
    }

}
