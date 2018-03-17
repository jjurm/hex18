package com.treecio.hexplore.ble

import android.app.Service
import android.content.Intent
import android.os.IBinder
import timber.log.Timber


class BleService : Service() {

    companion object {
        const val EXTRA_ACTION = "extra_action"
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    lateinit var scheduler: StateScheduler

    override fun onCreate() {
        super.onCreate()

        val stateA = BleBroadcastingState(this)
        val stateB = BleDiscoveryState(this)
        stateA.prepare()
        stateB.prepare()
        scheduler = SimpleStateScheduler(stateA, stateB)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Starting service")

        val action = intent?.getStringExtra(EXTRA_ACTION)
        when (action) {
            ACTION_START -> {
                scheduler.start()
            }
            ACTION_STOP -> {
                scheduler.stop()
                stopSelf()
            }
        }

        return START_STICKY
    }

}
