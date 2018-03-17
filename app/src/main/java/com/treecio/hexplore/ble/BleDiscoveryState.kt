package com.treecio.hexplore.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.widget.Toast
import com.raizlabs.android.dbflow.data.Blob
import com.treecio.hexplore.model.User
import com.treecio.hexplore.network.NetworkClient
import com.treecio.hexplore.utils.toHexString
import timber.log.Timber
import java.util.*


class BleDiscoveryState(context: Context) : BleAbstractState(context) {

    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var networkClient: NetworkClient

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { onSingleResult(it) }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            results.forEach(::onSingleResult)
        }

        fun onSingleResult(result: ScanResult) {
            result.device ?: return
            result.scanRecord.serviceData.values.forEach { remoteDeviceId ->
                Timber.i("Scanned ${remoteDeviceId.toHexString()}")
                handleId(Blob(remoteDeviceId))
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("Discovery onScanFailed: $errorCode")
        }
    }

    private fun startDiscovery() {
        val filters = listOf(
                ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(getServiceUuid()))
                        .build()
        )
        val settings = ScanSettings.Builder().setScanMode(BleConfig.SCAN_MODE).build()

        bluetoothLeScanner.startScan(filters, settings, scanCallback)
    }

    override fun prepare() {
        bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
        networkClient = NetworkClient(context)
    }

    private fun handleId(shortId: Blob) {
        val now = Date()
        val cal = Calendar.getInstance()
        cal.(BleConfig.thresholdDateLambda)()
        val thresholdDate = cal.time

        val user = User(shortId)
        var newUser = true
        if (user.exists()) {
            user.load()
            newUser = false
        }
        if (user.lastHandshake?.before(thresholdDate) ?: true) {
            val msg = "Handshake with " + shortId.blob.toHexString()
            Timber.d(msg)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            user.handshakeCount++
        }
        user.lastHandshake = now
        user.save()

        // if the user has not been queried yet
        if (newUser) {
            networkClient.queryUser(shortId.blob.toHexString())
        }
    }

    private fun stopDiscovery() {
        bluetoothLeScanner.stopScan(scanCallback);
    }


    override fun transitionIn() {
        Timber.d("Discovery: START")
        startDiscovery()
    }

    override fun transitionOut() {
        stopDiscovery()
        //Timber.d("Discovery: STOP")
    }
}
