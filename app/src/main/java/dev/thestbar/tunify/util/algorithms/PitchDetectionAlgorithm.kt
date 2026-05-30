package dev.thestbar.tunify.util.algorithms

fun interface PitchDetectionAlgorithm {
    fun getPitch(inputBuffer: ShortArray): Double
}
