package com.treecio.hexplore.activities

import android.support.v7.app.AppCompatActivity
import com.facebook.AccessToken
import com.treecio.hexplore.HexploreApp
import com.treecio.hexplore.permission.PermissionFlow

abstract class BaseActivity() : AppCompatActivity() {

    fun isLoginValid(): Boolean {
        return AccessToken.getCurrentAccessToken() != null
    }

    fun hasNecessaryPermissions(): Boolean {
        return HexploreApp.PERMISSIONS.all { PermissionFlow.check(this, it) }
    }

}
