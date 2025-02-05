# Bluetooth Tic-Tac-Toe (for Android 9 or less)

This simple application is needed to demonstrate the communication of two mobile devices via Bluetooth.
The application is written using the Jetpack Compose framework, but the point of the project is to test the possibility of creating offline games for two people.

## Why is it cool?

I honestly believe that what's missing in 2025 is games tied not to a connection to the cloud,
but to easy and convenient offline play with a friend in the absence of internet. It's hard to argue with how nice it is.

It's not the pinnacle of design or game mechanics - it's a demonstration of Bluetooth device communication technology.

## Technology

Bluetooth-wrapper is implemented as a Kotlin class:

```kotlin
class BluetoothManager(
    private val flagHost: (Boolean) -> Unit,     // Updates the device status - host or client
    private val onMoveReceived: (Int) -> Unit
) {
    // ...
}
```

After some preparation, we define the method by which the device enters host mode.

```kotlin
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
```

To access an existing host (client mode) another method is used:

```kotlin
suspend fun startClient(): Boolean = withContext(Dispatchers.IO) {
    bluetoothAdapter ?: return@withContext false

    flagHost(false)

    // Pick the first bonded device.
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
```

Very similar to the previous one, isn't it?

The next one will be much simpler: we send a data packet containing just one number to the other device.

```kotlin
fun sendMove(index: Int) {
    val outStream: OutputStream? = socket?.outputStream

    try {
        // Send the move as a single byte representing the index (0-8).
        outStream?.write(byteArrayOf(index.toByte()))
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
```

The last method is the heart of the whole technology.
We need to use a parallel thread to constantly check for incoming data packages.

```kotlin
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
```

And that's it!
