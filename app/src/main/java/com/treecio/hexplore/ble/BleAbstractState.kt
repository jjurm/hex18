package com.treecio.hexplore.ble

import android.content.Context
import com.treecio.hexplore.R
import java.util.*


abstract class BleAbstractState(val context: Context) : State {

    abstract fun prepare()

    protected fun getServiceUuid(): UUID =
            UUID.fromString(context.getString(R.string.ble_service_uuid))

    protected fun getDeviceIDBytes() = Preferences.getDeviceId(context)

}
