package com.junkiedan.junkietuner.util.algorithms;

/**
 * PitchDetectionAlgorithm Interface, it needs to be used
 * by any implemented Pitch Detection Algorithms because they need
 * to ensure that they contain the needed classes for the application
 * to work.
 * @author Stavros Barousis
 */
public interface PitchDetectionAlgorithm {
    double getPitch(short[] inputBuffer);
}
