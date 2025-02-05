package com.dinges.ttt_bluetooth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TicTacToeGameScreen(viewModel: TicTacToeViewModel) {
    // Observe connection state and board state from the view model.
    val connectionState = viewModel.connectionState.collectAsState()
    val board = viewModel.board.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (connectionState.value) {
            ConnectionState.Disconnected -> {
                Text("Not Connected")
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { viewModel.startBluetoothServer() }) {
                        Text("Host Game")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.startBluetoothClient() }) {
                        Text("Join Game")
                    }
                }
            }
            ConnectionState.Connecting -> {
                Text("Connecting...")
            }
            ConnectionState.Connected -> {
                Text("Connected! Your turn: ${viewModel.isMyTurn}")
                Spacer(modifier = Modifier.height(16.dp))
                TicTacToeBoard(board = board.value, onCellClicked = { index ->
                    viewModel.makeMove(index)
                })
            }
        }
    }
}

@Composable
fun TicTacToeBoard(board: List<CellState>, onCellClicked: (Int) -> Unit) {
    // Create a 3x3 grid.
    Column {
        for(i in 0..2) {

            Row {
                for(j in 0..2) {

                    val index = i * 3 + j

                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                            .clickable(enabled = board[index] == CellState.Empty) { onCellClicked(index) },
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = when(board[index]) {
                                    CellState.X -> "X"
                                    CellState.O -> "O"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                }
            }

        }
    }
}