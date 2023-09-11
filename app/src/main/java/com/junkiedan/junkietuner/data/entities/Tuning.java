package com.junkiedan.junkietuner.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
