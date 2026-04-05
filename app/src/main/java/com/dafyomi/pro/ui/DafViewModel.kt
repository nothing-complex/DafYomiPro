package com.dafyomi.pro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dafyomi.pro.DafYomiApp
import com.dafyomi.pro.domain.DafData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI State for the Daf screen.
 * @property isLoading True while fetching data
 * @property daf The fetched DafData, or null if not yet loaded or on error
 * @property error Error message string, or null if no error
 */
data class DafState(
    val isLoading: Boolean = true,
    val daf: DafData? = null,
    val error: String? = null
)

/**
 * ViewModel for the Daf Yomi screen.
 * Manages loading state and exposes DafData via StateFlow.
 *
 * Uses repository pattern to fetch data from Sefaria API.
 * All network operations run on Dispatchers.IO.
 */
class DafViewModel(
    private val repository: com.dafyomi.pro.domain.DafRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DafState())
    val state: StateFlow<DafState> = _state.asStateFlow()

    init {
        loadTodaysDaf()
    }

    /**
     * Loads today's Daf Yomi data.
     * Sets loading state, fetches from repository, updates state.
     */
    fun loadTodaysDaf() {
        viewModelScope.launch {
            _state.value = DafState(isLoading = true)
            try {
                val daf = withContext(Dispatchers.IO) {
                    repository.getTodaysDaf()
                }
                _state.value = DafState(isLoading = false, daf = daf)
            } catch (e: Exception) {
                _state.value = DafState(isLoading = false, error = e.message)
            }
        }
    }

    companion object {
        /**
         * Factory for creating DafViewModel with app-level repository.
         * Required for ViewModelProvider to instantiate the ViewModel.
         */
        fun factory(app: DafYomiApp): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DafViewModel(app.repository) as T
                }
            }
        }
    }
}
