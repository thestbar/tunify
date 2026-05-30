package dev.thestbar.tunify.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tuning(
    @JvmField @ColumnInfo(name = "name") var name: String,
    @JvmField @ColumnInfo(name = "notes") var notes: String
) {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun notesFormatted(): String {
        val sb = StringBuilder()
        val len = notes.length
        for (i in 1 until len - 1) {
            if (notes[i] == ',') sb.append("  ") else sb.append(notes[i])
        }
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tuning) return false
        return id == other.id && name == other.name && notes == other.notes
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + notes.hashCode()
        return result
    }
}
