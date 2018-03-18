package com.treecio.hexplore.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.widget.Toast
import com.treecio.hexplore.db.UsersReloadNeededEvent
import com.treecio.hexplore.model.User
import com.treecio.hexplore.network.NetworkClient
import com.treecio.hexplore.notification.NotificationBuilder
import com.treecio.hexplore.utils.toHexString
import org.greenrobot.eventbus.EventBus
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
                val hexString = remoteDeviceId.toHexString()
                Timber.i("Scanned ${hexString}")
                handleId(hexString)
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

    private fun handleId(shortId: String) {
        var newUser = true
        var showNotification = false
        // calculate dates
        val now = Date()
        val cal = Calendar.getInstance()
        cal.(BleConfig.thresholdDateLambda)()
        val thresholdDate = cal.time

        // load user if needed
        val user = User(shortId)
        if (user.exists()) {
            user.load()
            newUser = false
        }

        // update fields
        if (user.lastHandshake?.before(thresholdDate) ?: true) {
            val msg = "Handshake with " + shortId
            Timber.d(msg)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            user.handshakeCount++
            if (user.handshakeCount == BleConfig.HANDSHAKE_TARGET) {
                showNotification = true
            }
        }
        user.lastHandshake = now
        user.save()
        EventBus.getDefault().post(UsersReloadNeededEvent())

        // fetch facebook data
        if (newUser) {
            networkClient.queryUser(shortId)
        }
        if (showNotification) {
            NotificationBuilder(context).frequentPersonNotification(user)
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
