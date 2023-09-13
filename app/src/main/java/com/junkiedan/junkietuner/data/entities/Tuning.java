package com.junkiedan.junkietuner.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.junkiedan.junkietuner.data.TuningHandler;

import java.util.ArrayList;

@Entity
public class Tuning {
    @PrimaryKey(autoGenerate=true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "notes")
    public String notes;

    public Tuning(String name, String notes) {
        this.name = name;
        this.notes = notes;
    }

    public String notesFormatted() {
        StringBuilder sb = new StringBuilder();
        int len = notes.length();
        for (int i = 1; i < len - 1; ++i) {
            if (notes.charAt(i) == ',') {
                sb.append("  ");
            } else {
                sb.append(notes.charAt(i));
            }
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return "Tuning{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
