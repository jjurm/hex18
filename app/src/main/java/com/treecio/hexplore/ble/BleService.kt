package com.treecio.hexplore.ble

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import timber.log.Timber


class BleService : Service() {

    companion object {
        const val EXTRA_ACTION = "extra_action"
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    var scheduler: StateScheduler? = null

    private lateinit var stateA: BleBroadcastingState
    private lateinit var stateB: BleDiscoveryState

    override fun onCreate() {
        super.onCreate()

        stateA = BleBroadcastingState(this)
        stateB = BleDiscoveryState(this)
        stateA.prepare()
        stateB.prepare()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val action = intent?.getStringExtra(EXTRA_ACTION)
        when (action) {
            ACTION_START -> {
                Timber.d("Starting service")
                Toast.makeText(this, "Starting Service", Toast.LENGTH_SHORT).show()
                scheduler = SimpleStateScheduler(stateA, stateB)
                scheduler!!.start()
            }
            ACTION_STOP -> {
                Timber.d("Stopping service")
                Toast.makeText(this, "Stopping Service", Toast.LENGTH_SHORT).show()
                scheduler?.stop()
                stopSelf()
            }
        }

        return START_STICKY
    }

}
