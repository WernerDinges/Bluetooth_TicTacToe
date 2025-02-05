package com.dinges.ttt_bluetooth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class CellState { Empty, X, O }
enum class ConnectionState { Disconnected, Connecting, Connected }

class TicTacToeViewModel : ViewModel() {

    var isHost = false

    // Board state: a list of 9 cells (initially all empty).
    private val _board = MutableStateFlow(List(9) { CellState.Empty })
    val board: StateFlow<List<CellState>> = _board

    // Bluetooth connection state.
    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Indicates whether it’s the local player’s turn.
    var isMyTurn: Boolean = false
        private set

    // Instantiate the Bluetooth manager. It uses a callback to deliver remote moves.
    private val bluetoothManager = BluetoothManager(flagHost = { isHost = it }) { receivedMove ->
        // When a move is received via Bluetooth, update the board.
        onRemoteMove(receivedMove)
    }

    fun startBluetoothServer() {
        _connectionState.value = ConnectionState.Connecting
        viewModelScope.launch {
            // Start Bluetooth server in a background thread.
            val success = bluetoothManager.startServer()
            if (success) {
                _connectionState.value = ConnectionState.Connected
                // As host, you start the game.
                isMyTurn = true
            } else {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    fun startBluetoothClient() {
        _connectionState.value = ConnectionState.Connecting
        viewModelScope.launch {
            // Start Bluetooth client (using a bonded device).
            val success = bluetoothManager.startClient()
            if (success) {
                _connectionState.value = ConnectionState.Connected
                // As client, wait for the host’s move.
                isMyTurn = false
            } else {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    fun makeMove(index: Int) {
        // Allow a move only if it is the local player's turn and the cell is empty.
        if (!isMyTurn || _board.value[index] != CellState.Empty) return

        // Update the local board state.
        val newBoard = _board.value.toMutableList()
        newBoard[index] = if(isHost) CellState.X else CellState.O
        _board.value = newBoard

        // Send the move over Bluetooth.
        bluetoothManager.sendMove(index)

        // Toggle turn.
        isMyTurn = false

        // (Optionally, insert win/draw detection here.)
    }

    private fun onRemoteMove(index: Int) {
        // Update the board based on the remote move.
        val newBoard = _board.value.toMutableList()
        // Since the remote move always plays the opposite mark, we assign accordingly.
        newBoard[index] = if (isMyTurn) CellState.O else CellState.X
        _board.value = newBoard

        // Now it’s our turn.
        isMyTurn = true
    }
}