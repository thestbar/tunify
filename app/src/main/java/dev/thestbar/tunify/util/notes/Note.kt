package dev.thestbar.tunify.util.notes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(val name: String, val frequency: Double) : Parcelable
