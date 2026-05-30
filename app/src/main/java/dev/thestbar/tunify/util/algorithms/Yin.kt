package dev.thestbar.tunify.util.algorithms

/**
 * Yin Pitch Detection Algorithm (de Cheveigné & Kawahara, 2002).
 * Each instance holds its own working buffer; no global singleton.
 */
class Yin(private val sampleRate: Double) : PitchDetectionAlgorithm {

    private var inputBuffer: ShortArray = ShortArray(0)
    private var yinBuffer: DoubleArray = DoubleArray(0)

    override fun getPitch(inputBuffer: ShortArray): Double {
        this.inputBuffer = inputBuffer
        if (yinBuffer.size != inputBuffer.size / 2) {
            yinBuffer = DoubleArray(inputBuffer.size / 2)
        }

        difference()
        cumulativeMeanNormalizedDifference()
        val threshold = absoluteThreshold()

        return if (threshold != -1) {
            val optimized = parabolicInterpolation(threshold)
            sampleRate / optimized
        } else {
            -1.0
        }
    }

    private fun difference() {
        val len = yinBuffer.size
        for (tau in 0 until len) yinBuffer[tau] = 0.0
        for (tau in 1 until len) {
            for (j in 0 until len) {
                val diff = inputBuffer[j].toDouble() - inputBuffer[j + tau].toDouble()
                yinBuffer[tau] += diff * diff
            }
        }
    }

    private fun cumulativeMeanNormalizedDifference() {
        val len = yinBuffer.size
        yinBuffer[0] = 1.0
        var currSum = yinBuffer[1]
        yinBuffer[1] = 1.0
        for (tau in 2 until len) {
            currSum += yinBuffer[tau]
            yinBuffer[tau] *= tau / currSum
        }
    }

    private fun absoluteThreshold(): Int {
        val len = yinBuffer.size
        var tau = 2
        while (tau < len) {
            if (yinBuffer[tau] < GLOBAL_MINIMUM_THRESHOLD) {
                while (tau + 1 < len && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tau++
                }
                return tau
            }
            tau++
        }
        return -1
    }

    private fun parabolicInterpolation(threshold: Int): Double {
        val x0 = if (threshold < 1) threshold else threshold - 1
        val x2 = if (threshold + 1 < yinBuffer.size) threshold + 1 else threshold
        if (x0 == threshold) {
            return if (yinBuffer[threshold] <= yinBuffer[x2]) threshold.toDouble() else x2.toDouble()
        }
        if (x2 == threshold) {
            return if (yinBuffer[threshold] <= yinBuffer[x0]) threshold.toDouble() else x0.toDouble()
        }
        val s0 = yinBuffer[x0]
        val s1 = yinBuffer[threshold]
        val s2 = yinBuffer[x2]
        return threshold + 0.5 * (s2 - s0) / (2.0 * s1 - s2 - s0)
    }

    companion object {
        private const val GLOBAL_MINIMUM_THRESHOLD = 0.15
    }
}
