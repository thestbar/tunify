package dev.thestbar.tunify.core.fragments

import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.anastr.speedviewlib.components.Section
import com.github.anastr.speedviewlib.components.Style
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.thestbar.tunify.R
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.core.RecordingRunnable
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentMainBinding
import dev.thestbar.tunify.util.algorithms.NoteDetection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    private val recordingInProgress = AtomicBoolean(false)
    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var buffer: ShortArray? = null
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionToRecordAccepted = PackageManager.PERMISSION_GRANTED ==
            requireContext().checkCallingOrSelfPermission(android.Manifest.permission.RECORD_AUDIO)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPitchTextView()
        initSpeedView()
        initNotesTextViewList()
        initSelectedTuning()
        initTuningSwitch()
    }

    override fun onStart() {
        super.onStart()
        stopRecording()
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val isLoadLastMutedState = PreferencesDataStoreHandler
                    .getIsLoadLastMutedState(requireContext())
                    .first() ?: false
                val isTuning = PreferencesDataStoreHandler
                    .getIsTuning(requireContext())
                    .first() ?: false
                if (isLoadLastMutedState && isTuning) {
                    startRecording()
                }
            } catch (e: NullPointerException) {
                Log.w("MainFragment@onResume",
                    "IS_LOAD_LAST_MUTED_STATE or IS_TUNING not initialized")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initPitchTextView() {
        binding.textViewPitch.text = ""
    }

    private fun initSpeedView() {
        val sv = binding.speedView
        sv.minSpeed = -50f
        sv.maxSpeed = 50f

        sv.clearSections()
        val speedViewSectionColorId = ContextCompat.getColor(requireContext(), R.color.custom_vanilla)
        val mainSection = Section(0f, 1f, speedViewSectionColorId)
        mainSection.style = Style.ROUND
        sv.addSections(mainSection)
        sv.speedometerWidth = 8f

        sv.marksNumber = 9
        sv.markStyle = Style.ROUND
        sv.marksPadding = 5f
        sv.markHeight = 10f

        sv.tickNumber = 11
        sv.tickPadding = 20f
    }

    private fun initNotesTextViewList() {
        // Note text views are accessed directly via binding; nothing to pre-build here
    }

    private fun initSelectedTuning() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentTuningId = try {
                PreferencesDataStoreHandler.getCurrentTuningId(requireContext()).first() ?: -1
            } catch (e: NullPointerException) {
                Log.w("MainFragment@initSelectedTuning",
                    "getCurrentTuningId returned no value; defaulting to -1")
                -1
            }

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getTuningById(currentTuningId).collect { tuning ->
                    val resolvedTuning = tuning ?: Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]")
                    val guitarTuning = TuningHandler.getGuitarTuningFromTuning(resolvedTuning)
                    val noteTextViews = listOf(
                        binding.textViewNote1, binding.textViewNote2, binding.textViewNote3,
                        binding.textViewNote4, binding.textViewNote5, binding.textViewNote6
                    )
                    noteTextViews.forEachIndexed { i, tv ->
                        tv.text = guitarTuning.notes[i].name
                    }

                    val isTunerLocked = try {
                        PreferencesDataStoreHandler.getIsTunerLocked(requireContext()).first() ?: false
                    } catch (e: NullPointerException) {
                        Log.e("MainFragment@initSelectedTuning",
                            "getIsTunerLocked returned no value; defaulting to false")
                        false
                    }
                    if (isTunerLocked) {
                        val newNoteDetection = NoteDetection(guitarTuning.notes)
                        RecordingRunnable.setNoteDetection(newNoteDetection)
                    }
                }
            }
        }
    }

    private fun initTuningSwitch() {
        if (!permissionToRecordAccepted) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tuner has no access to microphone")
                .setMessage(
                    "In order to use this tuner you need to grant access to the " +
                    " application for the microphone of the device. Go to settings, " +
                    "grant access manually to the device's microphone and restart the" +
                    " application."
                )
                .setNegativeButton("Close the application") { _, _ ->
                    requireActivity().finish()
                }
                .setCancelable(false)
                .show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val isLoadLastMutedState = PreferencesDataStoreHandler
                    .getIsLoadLastMutedState(requireContext())
                    .first() ?: false
                val isTuning = PreferencesDataStoreHandler
                    .getIsTuning(requireContext())
                    .first() ?: false
                setSwitchChecked(isLoadLastMutedState && isTuning)
            } catch (e: NullPointerException) {
                setSwitchChecked(false)
                PreferencesDataStoreHandler.setIsLoadLastMutedState(requireContext(), true)
                PreferencesDataStoreHandler.setIsTuning(requireContext(), false)
            }
        }

        binding.tuningSwitch.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                PreferencesDataStoreHandler.setIsTuning(requireContext(), binding.tuningSwitch.isChecked)
            }
            if (binding.tuningSwitch.isChecked) {
                startRecording()
            } else {
                stopRecording()
            }
        }
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION
            )
            return
        }
        val buf = ShortArray(BUFFER_SIZE)
        buffer = buf
        val rec = AudioRecord(
            MediaRecorder.AudioSource.UNPROCESSED,
            SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )
        recorder = rec
        rec.startRecording()
        recordingInProgress.set(true)
        recordingThread = Thread(
            RecordingRunnable(
                requireActivity(),
                recordingInProgress,
                rec,
                binding.textViewPitch,
                binding.speedView,
                buf
            ),
            "Recording Thread"
        ).also { it.start() }
        binding.tuningSwitch.text = SWITCH_TURNED_ON_STR
    }

    private fun stopRecording() {
        val rec = recorder ?: return
        recordingInProgress.set(false)
        rec.stop()
        rec.release()
        recorder = null
        recordingThread = null
        binding.textViewPitch.text = ""
        binding.tuningSwitch.text = SWITCH_TURNED_OFF_STR
        binding.speedView.speedTo(0f, NEEDLE_ANIMATION_SPEED)
    }

    private fun setSwitchChecked(value: Boolean) {
        binding.tuningSwitch.isChecked = value
        viewLifecycleOwner.lifecycleScope.launch {
            PreferencesDataStoreHandler.setIsTuning(requireContext(), value)
        }
    }

    companion object {
        const val NEEDLE_ANIMATION_SPEED = 300L

        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val SAMPLING_RATE_IN_HZ = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 4
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT
        ) * BUFFER_SIZE_FACTOR

        private const val SWITCH_TURNED_ON_STR = "Tuning"
        private const val SWITCH_TURNED_OFF_STR = "Muted"

        fun newInstance(): MainFragment = MainFragment()
    }
}
