package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LuminaApp
import com.example.ui.LuminaViewModel
import com.example.ui.theme.LuminaBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Edge-to-Edge display system
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                // Outer window structure colored with Space-Dark Lumina Background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LuminaBackground
                ) {
                    val viewModel: LuminaViewModel = viewModel()
                    LuminaApp(viewModel = viewModel)
                }
            }
        }
    }
}
