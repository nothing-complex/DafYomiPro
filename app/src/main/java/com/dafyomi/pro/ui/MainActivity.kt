package com.dafyomi.pro.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dafyomi.pro.DafYomiApp
import com.dafyomi.pro.ui.theme.DafYomiProTheme
import com.dafyomi.pro.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DafYomiApp
        var themeMode by mutableStateOf(ThemeMode.OFF)

        setContent {
            DafYomiProTheme(themeMode = themeMode) {
                DafScreen(
                    viewModel = viewModel(
                        factory = DafViewModel.factory(app)
                    ),
                    onThemeModeChange = { newMode ->
                        themeMode = newMode
                    }
                )
            }
        }
    }
}
