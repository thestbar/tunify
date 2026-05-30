package dev.thestbar.tunify.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.entities.Tuning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder { DEFAULT, NAME_ASC, NAME_DESC, ID_DESC }

class TuningViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TuningRepository(application)

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)

    val allTunings: StateFlow<List<Tuning>> = repository.getAllTunings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val filteredTunings: StateFlow<List<Tuning>> = combine(
        allTunings,
        _searchQuery,
        _sortOrder
    ) { tunings, query, order ->
        val filtered = if (query.isBlank()) tunings
                       else tunings.filter { it.name.contains(query, ignoreCase = true) }
        when (order) {
            SortOrder.DEFAULT   -> filtered
            SortOrder.NAME_ASC  -> filtered.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            SortOrder.ID_DESC   -> filtered.sortedByDescending { it.id }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList()
    )

    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }

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
