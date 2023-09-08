package com.junkiedan.junkietuner.util.notes;

import androidx.annotation.NonNull;

/**
 * Model class that contains all the necessary data of a Note (name and frequency).
 * @author Stavros Barpusis
 */
public class Note {

    private final String name;
    private final double frequency;

    public Note(String name, double frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    public double getFrequency() {
        return frequency;
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{'" + name  + "': " + frequency + '}';
    }
}
