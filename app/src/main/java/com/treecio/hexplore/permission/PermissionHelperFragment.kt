package com.treecio.summapp.permission

import android.content.Intent
import android.support.v4.app.Fragment
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * This fragment serves as the creator and callback of permission requests. The fragment does not
 * contain any visible UI and is attached to an activity only to mediate the permission flow.
 */
class PermissionHelperFragment : Fragment() {

    private var permissionFlow: PermissionFlow? = null

    fun bindPermissionFlow(permissionFlow: PermissionFlow) {
        this.permissionFlow = permissionFlow
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // DEBUG
        if (permissionFlow == null) {
            // might become null after screen rotation?
            // https://fabric.io/treecio/android/apps/com.treecio.summapp.beta/issues/5a78e7148cb3c2fa63626b43
            Timber.w("onRequestPermissionsResult: permissionFlow is null")
            Crashlytics.log("onRequestPermissionsResult: permissionFlow is null")
        }
        permissionFlow!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!permissionFlow!!.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
