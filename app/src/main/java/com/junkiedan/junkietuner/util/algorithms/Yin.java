package com.junkiedan.junkietuner.util.algorithms;

/**
 * This class contains an implementation of the Yin Pitch Detection Algorithm.
 * More information on the algorithm can be found on the detailed implementation paper
 * which was written by Hideki Kawahara
 * (<a href="http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf">link here</a>).
 * This algorithm was developed to be used in a guitar tuner for Android platform.
 * @author Stavros Barousis
 */
public class Yin implements PitchDetectionAlgorithm{

    /**
     * The one and only active instance of Yin class that can exist.
     * Singleton pattern is used. Initially the reference points to null.
     */
    private static Yin classInstance = null;
    /**
     * inputBuffer contains a reference to the raw input data.
     */
    private short[] inputBuffer = null;
    /**
     * yinBuffer stores the calculated values of the Yin algorithm.
     * The size of this buffer is half the size of inputBuffer.
     */
    private double[] yinBuffer = null;
    /**
     * Global minimum threshold value. Check paper for more information
     * of this variable. It is mentioned that its value should be between
     * 0.1 and 0.15.
     */
    private final double GLOBAL_MINIMUM_THRESHOLD = 0.15;
    /**
     * The sample rate of the input data
     */
    private final double sampleRate;

    /**
     * Private constructor in order to have only 1 instantiated
     * object of this class.
     * @param sampleRate Value of the sample rate of the input data
     */
    private Yin(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * Function that returns the active instance of the Yin algorithm.
     * If it is not created yet, then initial the instance is invoked
     * and then returned.
     * @param sampleRate Value of the sample rate of the input data
     * @return Active instance of Yin algorithm.
     */
    public static Yin getInstance(int sampleRate) {
        if (classInstance == null) {
            classInstance = new Yin(sampleRate);
        }
        assert ((double) sampleRate == classInstance.sampleRate) : "ERROR: Input sample rate was changed." +
                "Given sampleRate: " + (double) sampleRate + " classInstance.sampleRate: " +
                classInstance.sampleRate;
        return classInstance;
    }

    /**
     * @param inputBuffer The raw input data provided to be analyzed.
     * @return Dominant frequency (pitch) of the given sample in frequency spectrum (Hz).
     * In case no pitch value is detected value -1 is returned.
     */
    @Override
    public double getPitch(short[] inputBuffer) {
        // Reference inputBuffer to this.inputBuffer
        this.inputBuffer = inputBuffer;
        // Initialize yinBuffer in case it was not initialized earlier.
        // It is expected that the inputBuffer will remain on constant length
        // on the whole lifespan of the application instance.
        if (yinBuffer == null || yinBuffer.length != inputBuffer.length / 2) {
            yinBuffer = new double[inputBuffer.length / 2];
        }

        double pitchInHz = -1;

        // Step 2
        difference();

        // Step 3
        cumulativeMeanNormalizedDifference();

        // Step 4
        int threshold = absoluteThreshold();

        if (threshold != -1) {
            // Step 5
            double optimizedThreshold = parabolicInterpolation(threshold);

            // Step 6
            // TODO - Implement Step 6 of the algorithm.
            // bestLocalEstimate();

            // Convert pitch to Hertz
            pitchInHz = sampleRate / optimizedThreshold;
        }

        return pitchInHz;
    }

    /**
     * Difference function is implemented as described in step 2
     * of the Yin algorithm paper.
     */
    private void difference() {
        int yinBufferLength = yinBuffer.length;
        int tau;
        int j;
        double diff;
        for (tau = 0; tau < yinBufferLength; ++tau) {
            yinBuffer[tau] = 0;
        }
        for (tau = 1; tau < yinBufferLength; ++tau) {
            for (j = 0; j < yinBufferLength; ++j) {
                diff = (double) inputBuffer[j] - (double) inputBuffer[j + tau];
                yinBuffer[tau] += diff * diff;
            }
        }
    }

    /**
     * Cumulative Mean Normalized Difference function as described
     * in step 3 of the Yin algorithm paper. Step 3 is optimized to have
     * in O(n) time complexity.
     */
    private void cumulativeMeanNormalizedDifference() {
        int yinBufferLength = yinBuffer.length;
        int tau;
        yinBuffer[0] = 1;
        double currSum = yinBuffer[1];
        yinBuffer[1] = 1;
        for (tau = 2; tau < yinBufferLength; ++tau) {
            currSum += yinBuffer[tau];
            yinBuffer[tau] *= tau / currSum;
        }
    }

    /**
     * Absolute Threshold function as described in step 4 of the
     * Yin algorithm paper.
     * @return Smallest value of tau that gives a minimum of
     * yinBuffer deeper than that threshold. If none is found
     * -1 value is returned. This will invoke the usage of the
     * global minimum value selected by the algorithm
     * (GLOBAL_MINIMUM_THRESHOLD variable).
     */
    private int absoluteThreshold() {
        int yinBufferLength = yinBuffer.length;
        int tau;
        for (tau = 2; tau < yinBufferLength; ++tau) {
            if (yinBuffer[tau] < GLOBAL_MINIMUM_THRESHOLD) {
                while (tau + 1 < yinBufferLength && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    ++tau;
                }
                return tau;
            }
        }
        return -1;
    }

    /**
     * Parabolic Interpolation function as described in step 5
     * of the Yin algorithm paper. This function refines the estimated
     * tau value using parabolic interpolation. This is necessary when
     * algorithm is trying to detect higher frequencies with bigger precision.
     * @param threshold Minimum absolute threshold calculated in step 4
     *                  (absolute threshold function). The maximum value
     *                  it can have is referenced in GLOBAL_MINIMUM_THRESHOLD
     *                  variable.
     * @return Optimized and more precise threshold value after performing
     *         parabolic interpolation.
     */
    private double parabolicInterpolation(int threshold) {
        int x0 = (threshold < 1) ? threshold : threshold - 1;
        int x2 = (threshold + 1 < yinBuffer.length) ? threshold + 1 : threshold;
        if (x0 == threshold) {
            return (yinBuffer[threshold] <= yinBuffer[x2]) ? (double) threshold : (double) x2;
        }
        if (x2 == threshold) {
            return (yinBuffer[threshold] <= yinBuffer[x0]) ? (double) threshold : (double) x0;
        }
        double s0 = yinBuffer[x0];
        double s1 = yinBuffer[threshold];
        double s2 = yinBuffer[x2];
        return threshold + 0.5f * (s2 - s0) / (2.0f * s1 - s2 - s0);
    }
}
