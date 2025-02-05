package com.dinges.ttt_bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * A simplified Bluetooth manager intended for Android 9 (API 28) and above.
 * It assumes that the necessary Bluetooth permissions are declared in the manifest.
 */
class BluetoothManager(
    private val flagHost: (Boolean) -> Unit,
    private val onMoveReceived: (Int) -> Unit
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var serverSocket: BluetoothServerSocket? = null

    private val APP_UUID: UUID = UUID.randomUUID()
    private val SERVICE_NAME = "TicTacToeBluetoothService"

    /**
     * Starts the device as a Bluetooth server.
     * This method blocks until a connection is accepted.
     */
    @SuppressLint("MissingPermission")
    suspend fun startServer(): Boolean = withContext(Dispatchers.IO) {
        bluetoothAdapter ?: return@withContext false

        flagHost(true)

        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
            socket = serverSocket?.accept() // Blocking call.
            serverSocket?.close() // Close server socket once connected.
            // Start listening for incoming moves.
            listenForMoves(socket)
            return@withContext true
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Starts the device as a Bluetooth client.
     * This example selects the first bonded device.
     */
    @SuppressLint("MissingPermission")
    suspend fun startClient(): Boolean = withContext(Dispatchers.IO) {
        bluetoothAdapter ?: return@withContext false

        flagHost(false)

        // For demonstration, pick the first bonded device.
        val device: BluetoothDevice = bluetoothAdapter.bondedDevices.firstOrNull() ?: return@withContext false

        try {
            socket = device.createRfcommSocketToServiceRecord(APP_UUID)
            socket?.connect()
            // Start listening for incoming moves.
            listenForMoves(socket)
            return@withContext true
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Sends a move (cell index) to the connected device.
     */
    @SuppressLint("MissingPermission")
    fun sendMove(index: Int) {
        val outStream: OutputStream? = socket?.outputStream
        try {
            // Send the move as a single byte representing the index (0-8).
            outStream?.write(byteArrayOf(index.toByte()))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Listens for moves from the connected device on a background thread.
     */
    private fun listenForMoves(socket: BluetoothSocket?) {
        socket ?: return

        Thread {
            try {
                val inStream: InputStream = socket.inputStream
                val buffer = ByteArray(1)
                while (true) {
                    val bytesRead = inStream.read(buffer)
                    if (bytesRead > 0) {
                        // Convert the received byte to an integer move.
                        val receivedIndex = buffer[0].toInt()
                        // Deliver the move via the callback.
                        onMoveReceived(receivedIndex)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }
}