package com.dafyomi.pro.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dafyomi.pro.DafYomiApp
import com.dafyomi.pro.ui.theme.DafYomiProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DafYomiApp

        setContent {
            DafYomiProTheme {
                DafScreen(
                    viewModel = viewModel(
                        factory = DafViewModel.factory(app)
                    )
                )
            }
        }
    }
}
