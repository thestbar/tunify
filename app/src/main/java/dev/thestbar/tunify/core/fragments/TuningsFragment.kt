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
import androidx.recyclerview.widget.LinearLayoutManager
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.core.TuningAdapter
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentTuningsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TuningsFragment : Fragment() {

    private var _binding: FragmentTuningsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTuningsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val initSelectedItemId = PreferencesDataStoreHandler
                .getCurrentTuningId(requireContext())
                .first() ?: -1

            val adapter = TuningAdapter(
                tuningList = emptyList(),
                fragmentManager = childFragmentManager,
                selectedItemId = initSelectedItemId,
                viewModel = viewModel,
                coroutineScope = viewLifecycleOwner.lifecycleScope
            )
            binding.tuningsList.adapter = adapter
            binding.tuningsList.layoutManager = LinearLayoutManager(requireContext())

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allTunings.collect { tunings ->
                    adapter.setTuningList(tunings)
                }
            }
        }

        binding.addTuningButton.setOnClickListener {
            AddTuningDialogFragment().show(childFragmentManager, "AddTuningDialogFragment")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): TuningsFragment = TuningsFragment()
    }
}
