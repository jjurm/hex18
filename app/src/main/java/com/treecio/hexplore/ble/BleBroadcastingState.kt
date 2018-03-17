package com.treecio.hexplore.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import timber.log.Timber

class BleBroadcastingState(context: Context) : BleAbstractState(context) {

    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser

    val advertisingCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            //Timber.i("Advertise success!")
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.e("Advertising onStartFailure: $errorCode")
        }
    }

    private fun startBroadcasting() {
        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(BleConfig.ADVERTISE_MODE)
                .setTxPowerLevel(BleConfig.ADVERTISE_POWER)
                .setConnectable(false)
                .build()

        val pUuid = ParcelUuid(getServiceUuid())

        val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, getDeviceIDBytes())
                .build()

        bluetoothLeAdvertiser.startAdvertising(settings, data, advertisingCallback)
    }

    private fun stopBroadcasting() {
        bluetoothLeAdvertiser.stopAdvertising(advertisingCallback)
    }

    override fun prepare() {
        bluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
    }


    override fun transitionIn() {
        //Timber.d("Broadcasting START")
        startBroadcasting()
    }
    override fun transitionOut() {
        stopBroadcasting()
        //Timber.d("Broadcasting STOP")
    }

}
