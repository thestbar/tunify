package dev.thestbar.tunify.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = view.context

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    PreferencesDataStoreHandler.getIsTunerLocked(ctx).collect { value ->
                        binding.lockTunerSwitch.isChecked = value == true
                    }
                }
                launch {
                    PreferencesDataStoreHandler.getIsLoadLastMutedState(ctx).collect { value ->
                        binding.loadLastMutedStateSwitch.isChecked = value == true
                    }
                }
            }
        }

        binding.lockTunerSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                PreferencesDataStoreHandler.setIsTunerLocked(ctx, isChecked)
            }
        }
        binding.loadLastMutedStateSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                PreferencesDataStoreHandler.setIsLoadLastMutedState(ctx, isChecked)
            }
        }

        binding.resetDatabaseTextView.isClickable = true
        binding.resetDatabaseTextView.setOnClickListener { v ->
            MaterialAlertDialogBuilder(v.context)
                .setTitle("Reset Tuning Database")
                .setMessage("All the changes that you made will be lost. The database will " +
                        "contain only the initial tunings. Do you still want to proceed?")
                .setPositiveButton("No") { dialog, _ -> dialog.dismiss() }
                .setNegativeButton("Yes") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        TuningHandler.resetDatabaseValuesToDefault(
                            TuningRepository(requireActivity().application)
                        )
                    }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
