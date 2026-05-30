package dev.thestbar.tunify.core.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentAddTuningDialogBinding
import dev.thestbar.tunify.util.notes.Note
import dev.thestbar.tunify.util.notes.NotesStructure

class AddTuningDialogFragment(
    private val tuning: Tuning = Tuning("", "[E2,A2,D3,G3,B3,E4]")
) : DialogFragment() {

    private var _binding: FragmentAddTuningDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTuningDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinners = listOf(
            binding.spinnerNote1, binding.spinnerNote2, binding.spinnerNote3,
            binding.spinnerNote4, binding.spinnerNote5, binding.spinnerNote6
        )

        val arrayAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            NotesStructure.notesAsStringArray
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.addTuningCancelButton.setOnClickListener { dismiss() }

        val guitarTuning = TuningHandler.getGuitarTuningFromTuning(tuning)
        binding.newTuningNameInput.setText(tuning.name)

        spinners.forEachIndexed { i, spinner ->
            spinner.adapter = arrayAdapter
            val idx = NotesStructure.searchNoteIndex(guitarTuning.notes[i].name)
            spinner.setSelection(idx)
        }

        binding.addTuningOkButton.setOnClickListener {
            tuning.name = binding.newTuningNameInput.text?.toString()?.trim().orEmpty()
            if (tuning.name.isEmpty()) {
                binding.newTuningNameInput.error = "Tuning Name is Required!"
                binding.newTuningNameInput.hint = "Enter Tuning Name"
                return@setOnClickListener
            }
            val notes: Array<Note> = Array(spinners.size) { i ->
                NotesStructure.searchNote(spinners[i].selectedItem.toString())!!
            }
            tuning.notes = TuningHandler.getNotesStringFromNotesArray(notes)
            viewModel.insert(tuning)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        // intentionally empty - keeps the window non-cancellable from outside taps
    }
}
