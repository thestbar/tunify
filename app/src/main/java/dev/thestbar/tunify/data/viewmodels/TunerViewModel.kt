package dev.thestbar.tunify.data.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.core.RecordingRunnable
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.util.algorithms.NoteDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TunerUiState(
    val detectedNote: String = "",
    val centsOffset: Float = 0f,
    val isTuning: Boolean = false,
    val currentTuning: Tuning = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]"),
    val currentTuningStrings: List<String> = listOf("E2", "A2", "D3", "G3", "B3", "E4"),
    val hasAudioPermission: Boolean = false
)

class TunerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val tuningRepository = TuningRepository(application)

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    val selectedTuningId: StateFlow<Int> =
        PreferencesDataStoreHandler.getCurrentTuningId(context)
            .map { it ?: -1 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), -1)

    private var recorder: AudioRecord? = null
    private var recordingJob: Job? = null

    init {
        viewModelScope.launch { loadInitialState() }
        observeCurrentTuning()
    }

    private fun observeCurrentTuning() {
        viewModelScope.launch {
            selectedTuningId
                .flatMapLatest { id -> tuningRepository.getTuningById(id) }
                .collect { tuning ->
                    val resolved = tuning ?: Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]")
                    val guitarTuning = TuningHandler.getGuitarTuningFromTuning(resolved)
                    val isTunerLocked = try {
                        PreferencesDataStoreHandler.getIsTunerLocked(context).first() ?: false
                    } catch (e: NullPointerException) { false }
                    if (isTunerLocked) {
                        RecordingRunnable.setNoteDetection(NoteDetection(guitarTuning.notes))
                    }
                    _uiState.update { it.copy(
                        currentTuning = resolved,
                        currentTuningStrings = guitarTuning.notes.map { n -> n.name }
                    ) }
                }
        }
    }

    private suspend fun loadInitialState() {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val isLoadLastMutedState = try {
            PreferencesDataStoreHandler.getIsLoadLastMutedState(context).first() ?: false
        } catch (e: NullPointerException) { false }

        val isTuning = try {
            PreferencesDataStoreHandler.getIsTuning(context).first() ?: false
        } catch (e: NullPointerException) { false }

        _uiState.update { it.copy(hasAudioPermission = hasPermission) }

        if (hasPermission && isLoadLastMutedState && isTuning) {
            startRecording()
        }
    }

    fun setIsTuning(value: Boolean) {
        viewModelScope.launch {
            PreferencesDataStoreHandler.setIsTuning(context, value)
        }
        if (value) startRecording() else stopRecording()
    }

    fun selectTuning(tuningId: Int) {
        viewModelScope.launch {
            PreferencesDataStoreHandler.setCurrentTuningId(context, tuningId)
        }
    }

    fun onPermissionGranted() {
        _uiState.update { it.copy(hasAudioPermission = true) }
        viewModelScope.launch {
            val isLoadLastMutedState = try {
                PreferencesDataStoreHandler.getIsLoadLastMutedState(context).first() ?: false
            } catch (e: NullPointerException) { false }
            val isTuning = try {
                PreferencesDataStoreHandler.getIsTuning(context).first() ?: false
            } catch (e: NullPointerException) { false }
            if (isLoadLastMutedState && isTuning) startRecording()
        }
    }

    private fun startRecording() {
        if (recordingJob?.isActive == true) return
        if (ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val buf = ShortArray(BUFFER_SIZE)
        val rec = AudioRecord(
            MediaRecorder.AudioSource.UNPROCESSED,
            SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )
        recorder = rec
        if (rec.state != AudioRecord.STATE_INITIALIZED) {
            rec.release()
            recorder = null
            return
        }
        rec.startRecording()
        _uiState.update { it.copy(isTuning = true) }

        val runnable = RecordingRunnable(rec, buf) { note, cents ->
            _uiState.update { it.copy(detectedNote = note, centsOffset = cents) }
        }
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            runnable.record()
        }
    }

    private fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        recorder?.stop()
        recorder?.release()
        recorder = null
        _uiState.update { it.copy(isTuning = false, detectedNote = "", centsOffset = 0f) }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }

    companion object {
        const val NEEDLE_ANIMATION_SPEED = 300L
        private const val SAMPLING_RATE_IN_HZ = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 4
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT
        ) * BUFFER_SIZE_FACTOR
    }
}
