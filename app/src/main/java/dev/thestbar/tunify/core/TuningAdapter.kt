package dev.thestbar.tunify.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.thestbar.tunify.R
import dev.thestbar.tunify.core.fragments.AddTuningDialogFragment
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.TuningListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TuningAdapter(
    private val fragmentManager: FragmentManager,
    private var selectedItemId: Int,
    private val viewModel: TuningViewModel,
    private val coroutineScope: CoroutineScope
) : ListAdapter<Tuning, TuningAdapter.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context

    inner class ViewHolder(val binding: TuningListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(TuningListItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tuning = getItem(position)

        holder.binding.tuningListItemLinearLayoutBackgroundId.setBackgroundResource(
            if (tuning.id == selectedItemId) R.color.custom_taupe_gray else R.color.custom_raisin_black
        )
        holder.binding.tuningName.text = tuning.name
        holder.binding.tuningNotes.text = tuning.notesFormatted()

        holder.binding.deleteButton.setOnClickListener {
            Log.d("TuningAdapter", "Deleting tuning: $tuning")
            viewModel.delete(tuning)
        }
        holder.binding.root.setOnLongClickListener {
            context.vibrate(60)
            AddTuningDialogFragment(tuning).show(fragmentManager, "EditTuningDialogFragment")
            true
        }
        holder.binding.root.setOnClickListener {
            context.vibrate(60)
            val previousId = selectedItemId
            selectedItemId = tuning.id
            val previousPos = currentList.indexOfFirst { it.id == previousId }
            if (previousPos != -1) notifyItemChanged(previousPos)
            notifyItemChanged(holder.bindingAdapterPosition)
            coroutineScope.launch {
                PreferencesDataStoreHandler.setCurrentTuningId(context, tuning.id)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Tuning>() {
            override fun areItemsTheSame(oldItem: Tuning, newItem: Tuning) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Tuning, newItem: Tuning) = oldItem == newItem
        }
    }
}

private fun Context.vibrate(milliseconds: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator?.vibrate(
            VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        @Suppress("DEPRECATION")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        @Suppress("DEPRECATION")
        vibrator?.vibrate(milliseconds)
    }
}
