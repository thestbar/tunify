package dev.thestbar.tunify.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.entities.Tuning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TuningViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TuningRepository(application)

    val allTunings: StateFlow<List<Tuning>> = repository.getAllTunings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    fun getTuningById(id: Int): Flow<Tuning?> = repository.getTuningById(id)

    fun insert(tuning: Tuning) {
        viewModelScope.launch { repository.insert(tuning) }
    }

    fun update(tuning: Tuning) {
        viewModelScope.launch { repository.update(tuning) }
    }

    fun delete(tuning: Tuning) {
        viewModelScope.launch { repository.delete(tuning) }
    }

    fun deleteAll() {
        viewModelScope.launch { repository.deleteAll() }
    }
}
