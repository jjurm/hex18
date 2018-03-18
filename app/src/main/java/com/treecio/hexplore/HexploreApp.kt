package com.treecio.hexplore

import android.Manifest
import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.facebook.stetho.Stetho
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.treecio.hexplore.ble.BleConfig
import com.treecio.hexplore.ble.Preferences
import com.treecio.hexplore.db.DBFlowInit
import com.treecio.hexplore.model.User
import com.treecio.hexplore.utils.toBytes
import com.treecio.hexplore.utils.toHexString
import timber.log.Timber
import java.util.*

class HexploreApp : Application() {

    companion object {

        val PERMISSIONS = listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
        ).toTypedArray()

    }

    override fun onCreate() {
        super.onCreate()

        // Timber
        Timber.plant(Timber.DebugTree())

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // DBFlow
        DBFlowInit.init(this)

        // Stetho
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }

        Timber.w("Local device id: " + Preferences.getDeviceId(this).toHexString())

        addMockData()
    }

    fun addMockData() {
        if ((select from User::class).list.size == 0) {
            val users = listOf(
                    User(
                            UUID.randomUUID().toBytes().take(BleConfig.MAX_BYTES).toByteArray().toHexString(),
                            8,
                            Date(System.currentTimeMillis() - 60_000 * 4),
                            "Someone Funny",
                            "https://texasbarblog.lexblogplatformtwo.com/files/2011/12/housto-bankruptcy-attorney-adam-schachter1.jpg",
                            "My Occupation",
                            "https://google.com"
                    )
            )
            users.forEach { it.save() }
        }
    }

}
