package com.junkiedan.junkietuner.util.notes;

import androidx.annotation.NonNull;

/**
 * Model class that contains all the necessary data of a Note (name and frequency).
 * @author Stavros Barpusis
 */
public class Note {

    // Name of the Note (For example A2).
    private final String name;
    // Double value of the frequency of the Note.
    // For example for A2 the frequency is 110.0 Hz.
    private final double frequency;

    /**
     * Public constructor of the Note class.
     * @param name The string of the name of the Note that will be created,
     * @param frequency The frequency of the Note (double value).
     */
    public Note(String name, double frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    /**
     * Getter for the name of the Note.
     * @return The string of the name of the Note.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the frequency of the Note.
     * @return The double value of the frequency of the Note.
     */
    public double getFrequency() {
        return frequency;
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{'" + name  + "': " + frequency + '}';
    }
}
