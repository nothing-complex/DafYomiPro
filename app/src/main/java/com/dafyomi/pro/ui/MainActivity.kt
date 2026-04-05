package com.dafyomi.pro.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
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
        var themeMode by mutableStateOf(ThemeMode.AUTO)

        setContent {
            DafYomiProTheme(themeMode = themeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
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
}
