package com.dinges.ttt_bluetooth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dinges.ttt_bluetooth.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {

    // Obtain a reference to our view model
    private val viewModel: TicTacToeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TicTacToeTheme {
                Surface(Modifier.fillMaxSize()) {

                    TicTacToeGameScreen(viewModel)

                }
            }
        }

    }
}