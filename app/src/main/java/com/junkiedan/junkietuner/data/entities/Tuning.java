package com.junkiedan.junkietuner.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Tuning entity that is used for the Room Database
 * @author Stavros Barousis
 */
@Entity
public class Tuning {

    // The unique ID of the tuning
    @PrimaryKey(autoGenerate=true)
    public int id;

    // Name of the tuning (For example "Standard E")
    @ColumnInfo(name = "name")
    public String name;

    // Notes string formatted as "[E2,A2,D3,G3,B3,E4]"
    @ColumnInfo(name = "notes")
    public String notes;

    public Tuning(String name, String notes) {
        this.name = name;
        this.notes = notes;
    }

    /**
     * Formatter function for the string that is displayed
     * on the user interface under the tunings fragment
     * @return Tuning in the UI format. For example the
     * "Standard E" Tuning "[E2,A2,D3,G3,B3,E4]" is returned as
     * "E2 A2 D3 G3 B3 E4" (between each notes there are 2
     * space characters.
     */
    public String notesFormatted() {
        StringBuilder sb = new StringBuilder();
        int len = notes.length();
        for (int i = 1; i < len - 1; ++i) {
            if (notes.charAt(i) == ',') {
                // Number of space characters that will be between
                // each note (Currently this value is set to 2.
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
