package dev.thestbar.tunify.core

import android.app.Activity
import android.media.AudioRecord
import android.widget.TextView
import com.github.anastr.speedviewlib.SpeedView
import dev.thestbar.tunify.util.algorithms.NoteDetection
import dev.thestbar.tunify.util.algorithms.Yin
import dev.thestbar.tunify.util.notes.NotesStructure
import java.util.concurrent.atomic.AtomicBoolean
import dev.thestbar.tunify.core.fragments.MainFragment.Companion.NEEDLE_ANIMATION_SPEED
import kotlin.math.roundToLong

class RecordingRunnable(
    private val mainActivity: Activity,
    private val recordingInProgress: AtomicBoolean,
    private val recorder: AudioRecord,
    private val pitchTextView: TextView,
    private val speedView: SpeedView,
    private val inputBuffer: ShortArray
) : Thread() {

    private val yinInstance = Yin(recorder.sampleRate.toDouble())

    init {
        if (noteDetection == null) {
            noteDetection = NoteDetection(NotesStructure.allNotes)
        }
    }

    override fun run() {
        val len = inputBuffer.size
        while (recordingInProgress.get()) {
            recorder.read(inputBuffer, 0, len)
            val pitchInHz = yinInstance.getPitch(inputBuffer)
            if (!pitchInHz.isFinite() || pitchInHz == -1.0) continue

            val nd = noteDetection ?: continue
            val closestNote = nd.findClosestNote(pitchInHz)
            val deltaInCents = NoteDetection.getDifferentInCents(closestNote, pitchInHz)

            mainActivity.runOnUiThread {
                pitchTextView.text = closestNote.name
                speedView.speedTo(deltaInCents.roundToLong().toFloat(), NEEDLE_ANIMATION_SPEED)
            }
        }
    }

    companion object {
        @Volatile
        private var noteDetection: NoteDetection? = null

        @JvmStatic
        @Synchronized
        fun setNoteDetection(newNoteDetection: NoteDetection) {
            noteDetection = newNoteDetection
        }
    }
}
