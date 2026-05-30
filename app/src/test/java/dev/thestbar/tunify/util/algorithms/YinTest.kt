package dev.thestbar.tunify.util.algorithms

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class YinTest {

    private val sampleRate = 44100.0

    @Test
    fun `detects 440 Hz sine wave within 1 Hz`() {
        val pitch = detect(440.0)
        assertEquals(440.0, pitch, 1.0)
    }

    @Test
    fun `detects 220 Hz sine wave within 1 Hz`() {
        val pitch = detect(220.0)
        assertEquals(220.0, pitch, 1.0)
    }

    @Test
    fun `detects 880 Hz sine wave within 2 Hz`() {
        val pitch = detect(880.0)
        assertEquals(880.0, pitch, 2.0)
    }

    @Test
    fun `silent input returns -1`() {
        val yin = Yin(sampleRate)
        val buffer = ShortArray(8192)
        val pitch = yin.getPitch(buffer)
        assertEquals(-1.0, pitch, 1e-9)
    }

    @Test
    fun `two instances do not share state`() {
        val a = Yin(44100.0)
        val b = Yin(22050.0)
        val buffer = sineWave(440.0, 8192, 44100.0)
        // a is at 44100 Hz so will detect ~440 Hz; b is at 22050 Hz so will detect ~220 Hz
        val pitchA = a.getPitch(buffer)
        val pitchB = b.getPitch(buffer)
        assertEquals(440.0, pitchA, 1.0)
        assertEquals(220.0, pitchB, 1.0)
    }

    private fun detect(frequency: Double): Double {
        val yin = Yin(sampleRate)
        val buffer = sineWave(frequency, 8192, sampleRate)
        return yin.getPitch(buffer)
    }

    private fun sineWave(frequency: Double, samples: Int, sampleRate: Double): ShortArray {
        val out = ShortArray(samples)
        val amplitude = Short.MAX_VALUE / 2
        for (i in 0 until samples) {
            out[i] = (amplitude * sin(2.0 * PI * frequency * i / sampleRate)).toInt().toShort()
        }
        return out
    }
}
