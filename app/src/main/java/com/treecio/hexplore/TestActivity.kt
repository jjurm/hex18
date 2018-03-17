package com.treecio.hexplore

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }

    override fun onStart() {
        super.onStart()

        /*btnBroadcast.setOnClickListener {
            val intent = Intent(this, BleService::class.java)
            intent.putExtra(BleService.EXTRA_MODE, BleService.MODE_BROADCAST)
            startService(intent)
        }
        btnDiscovery.setOnClickListener {
            val intent = Intent(this, BleService::class.java)
            intent.putExtra(BleService.EXTRA_MODE, BleService.MODE_DISCOVERY)
            startService(intent)
        }*/
    }

}
