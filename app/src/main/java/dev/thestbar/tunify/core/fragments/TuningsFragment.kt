package dev.thestbar.tunify.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dev.thestbar.tunify.R
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.core.TuningAdapter
import dev.thestbar.tunify.data.viewmodels.SortOrder
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentTuningsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TuningsFragment : Fragment() {

    private var _binding: FragmentTuningsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    private var adapter: TuningAdapter? = null
    private var scrollToTopPending = false

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

            adapter = TuningAdapter(
                fragmentManager = childFragmentManager,
                selectedItemId = initSelectedItemId,
                viewModel = viewModel,
                coroutineScope = viewLifecycleOwner.lifecycleScope
            )
            binding.tuningsList.adapter = adapter
            binding.tuningsList.layoutManager = LinearLayoutManager(requireContext())

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredTunings.collect { tunings ->
                    adapter?.submitList(tunings) {
                        if (scrollToTopPending) {
                            scrollToTopPending = false
                            binding.tuningsList.scrollToPosition(0)
                        }
                    }
                }
            }
        }

        binding.addTuningButton.setOnClickListener {
            AddTuningDialogFragment().show(childFragmentManager, "AddTuningDialogFragment")
        }

        binding.searchEditText.doAfterTextChanged { editable ->
            viewModel.setSearchQuery(editable?.toString() ?: "")
        }

        binding.sortButton.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menu.add(0, 0, 0, getString(R.string.sort_default))
            popup.menu.add(0, 1, 1, getString(R.string.sort_name_asc))
            popup.menu.add(0, 2, 2, getString(R.string.sort_name_desc))
            popup.menu.add(0, 3, 3, getString(R.string.sort_id_desc))
            popup.setOnMenuItemClickListener { item ->
                scrollToTopPending = true
                when (item.itemId) {
                    0 -> viewModel.setSortOrder(SortOrder.DEFAULT)
                    1 -> viewModel.setSortOrder(SortOrder.NAME_ASC)
                    2 -> viewModel.setSortOrder(SortOrder.NAME_DESC)
                    3 -> viewModel.setSortOrder(SortOrder.ID_DESC)
                }
                true
            }
            popup.show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.tuningsList) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        _binding = null
    }

    companion object {
        fun newInstance(): TuningsFragment = TuningsFragment()
    }
}
