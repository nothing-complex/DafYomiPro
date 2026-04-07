package com.dafyomi.pro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dafyomi.pro.DafYomiApp
import com.dafyomi.pro.domain.DafData
import com.dafyomi.pro.domain.SettingsManager
import com.dafyomi.pro.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

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
    private val repository: com.dafyomi.pro.domain.DafRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _state = MutableStateFlow(DafState())
    val state: StateFlow<DafState> = _state.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.AUTO)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _fontSizeMultiplier = MutableStateFlow(1.0f)
    val fontSizeMultiplier: StateFlow<Float> = _fontSizeMultiplier.asStateFlow()

    // Track the date we last loaded - used to detect day changes while app is open
    private var lastLoadedDate: LocalDate = LocalDate.MIN

    init {
        loadSavedSettings()
        loadTodaysDaf()
        startDateChangeDetector()
    }

    /**
     * Loads saved settings from DataStore.
     */
    private fun loadSavedSettings() {
        viewModelScope.launch {
            val savedTheme = settingsManager.themeModeFlow.first()
            _themeMode.value = savedTheme

            val savedFontSize = settingsManager.fontSizeFlow.first()
            _fontSizeMultiplier.value = savedFontSize
        }
    }

    /**
     * Updates and persists the theme mode.
     */
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            settingsManager.saveThemeMode(mode)
        }
    }

    /**
     * Updates and persists the font size multiplier.
     */
    fun setFontSizeMultiplier(multiplier: Float) {
        _fontSizeMultiplier.value = multiplier
        viewModelScope.launch {
            settingsManager.saveFontSize(multiplier)
        }
    }

    /**
     * Periodically checks if the date has changed since last load.
     * When the day rolls over while the app is open, reload the daf.
     */
    private fun startDateChangeDetector() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000) // Check every minute
                val today = LocalDate.now()
                if (today != lastLoadedDate) {
                    loadTodaysDaf()
                }
            }
        }
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
                lastLoadedDate = LocalDate.now()
                _state.value = DafState(isLoading = false, daf = daf)
            } catch (e: Exception) {
                _state.value = DafState(isLoading = false, error = e.message)
            }
        }
    }

    companion object {
        /**
         * Factory for creating DafViewModel with app-level repository and settings manager.
         * Required for ViewModelProvider to instantiate the ViewModel.
         */
        fun factory(app: DafYomiApp): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DafViewModel(app.repository, app.settingsManager) as T
                }
            }
        }
    }
}
