package dev.thestbar.tunify.core

import android.media.AudioRecord
import dev.thestbar.tunify.util.algorithms.NoteDetection
import dev.thestbar.tunify.util.algorithms.Yin
import dev.thestbar.tunify.util.notes.NotesStructure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

class RecordingRunnable(
    private val recorder: AudioRecord,
    private val inputBuffer: ShortArray,
    private val onPitchDetected: (note: String, centsOffset: Float) -> Unit
) {

    private val yinInstance = Yin(recorder.sampleRate.toDouble())

    init {
        if (noteDetection == null) {
            noteDetection = NoteDetection(NotesStructure.allNotes)
        }
    }

    suspend fun record() {
        val len = inputBuffer.size
        while (currentCoroutineContext().isActive) {
            recorder.read(inputBuffer, 0, len)
            val pitchInHz = yinInstance.getPitch(inputBuffer)
            if (!pitchInHz.isFinite() || pitchInHz == -1.0) continue
            val nd = noteDetection ?: continue
            val closestNote = nd.findClosestNote(pitchInHz)
            val deltaInCents = NoteDetection.getDifferentInCents(closestNote, pitchInHz)
            withContext(Dispatchers.Main) {
                onPitchDetected(closestNote.name, deltaInCents.roundToLong().toFloat())
            }
        }
    }

    companion object {
        @Volatile private var noteDetection: NoteDetection? = null

        @Synchronized
        fun setNoteDetection(newNoteDetection: NoteDetection) {
            noteDetection = newNoteDetection
        }
    }
}
