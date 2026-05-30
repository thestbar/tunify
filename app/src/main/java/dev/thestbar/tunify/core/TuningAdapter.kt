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
import androidx.recyclerview.widget.RecyclerView
import dev.thestbar.tunify.R
import dev.thestbar.tunify.core.fragments.AddTuningDialogFragment
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.TuningListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TuningAdapter(
    private var tuningList: List<Tuning>,
    private val fragmentManager: FragmentManager,
    private var selectedItemId: Int,
    private val viewModel: TuningViewModel,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<TuningAdapter.ViewHolder>() {

    private lateinit var context: Context

    fun setTuningList(tuningList: List<Tuning>) {
        this.tuningList = tuningList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: TuningListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun select() {
            binding.tuningListItemLinearLayoutBackgroundId
                .setBackgroundResource(R.color.custom_taupe_gray)
        }

        fun unselect() {
            binding.tuningListItemLinearLayoutBackgroundId
                .setBackgroundResource(R.color.custom_raisin_black)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = TuningListItemBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tuning = tuningList[holder.layoutPosition]
        viewHolderMap[tuning.id] = holder
        if (selectedItemId == tuning.id) holder.select() else holder.unselect()

        holder.binding.tuningName.text = tuning.name
        holder.binding.tuningNotes.text = tuning.notesFormatted()
        holder.binding.deleteButton.setOnClickListener {
            Log.d("Deleting Tuning From DB", tuning.toString())
            viewModel.delete(tuning)
        }
        holder.binding.root.setOnLongClickListener {
            context.vibrate(60)
            AddTuningDialogFragment(tuning).show(fragmentManager, "EditTuningDialogFragment")
            true
        }
        holder.binding.root.setOnClickListener {
            context.vibrate(60)
            Log.d(
                "TuningAdapter.onClick",
                "Value: $tuning has been selected as default Tuning"
            )
            viewHolderMap[selectedItemId]?.unselect()
            selectedItemId = tuning.id
            notifyItemChanged(holder.layoutPosition)
            holder.select()
            coroutineScope.launch {
                PreferencesDataStoreHandler.setCurrentTuningId(context, tuning.id)
            }
        }
    }

    override fun getItemCount(): Int = tuningList.size

    companion object {
        private val viewHolderMap: MutableMap<Int, ViewHolder> = HashMap()
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
