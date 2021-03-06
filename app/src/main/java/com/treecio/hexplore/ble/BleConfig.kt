package com.treecio.hexplore.ble

import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import java.util.*

object BleConfig {

    const val ADVERTISE_POWER = AdvertiseSettings.ADVERTISE_TX_POWER_LOW
    const val ADVERTISE_MODE = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
    const val SCAN_MODE = ScanSettings.SCAN_MODE_LOW_LATENCY
    const val NEARBY_THRESHOLD = 10

    const val MAX_BYTES = 9

    const val TIME_UNIT = 700

    val thresholdDateLambda: Calendar.() -> Unit = { add(Calendar.SECOND, -12) }

    val HANDSHAKE_TARGET = 3

}
