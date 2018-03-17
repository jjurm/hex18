package com.treecio.hexplore

import android.Manifest
import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.facebook.stetho.Stetho
import com.treecio.hexplore.db.DBFlowInit
import timber.log.Timber

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
    }

}
