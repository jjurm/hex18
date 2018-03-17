package com.treecio.summapp.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.treecio.hexplore.R
import java.util.*

typealias StringResource = Int

/**
 * Handles the full process regarding permissions - checking permissions, asking for permissions,
 * showing an rationale dialog offering to re-try, and notifying listeners. This class detects which
 * dialogs are supposed to be show and acts accordingly.
 *
 *
 * There are two modes of the permission flow:  * **Interactive** - the permission flow can
 * invoke dialogs * **Silent** - the permission flow will run callbacks immediately,
 * without any prompts to the user
 */
class PermissionFlow private constructor(
        private val context: Context,
        private val fragment: PermissionHelperFragment?,
        private val requestId: Int,
        val args: Bundle,
        private val permissions: Array<String>,
        @StringRes private val rationale: StringResource,
        private val shouldOpenSettingsIfNeeded: Boolean,
        private val silent: Boolean,
        private val callback: PermissionCallback?
) {

    companion object {

        const val DEFAULT_REQUEST_ID = -1

        private const val PHASE_BEFORE_RATIONALE = 0
        private const val PHASE_AFTER_RATIONALE = 1
        private const val PHASE_AFTER_SETTINGS = 2

        // request codes will be passed to Activity's onActivityResult, so make it random enough
        private const val REQUEST_CODES_BASE = 17000
        private const val REQUEST_OPEN_SETTINGS = 1

        /**
         * Constructs the builder. Enforces silent mode.
         *
         * @param context     context
         * @param permissions array of requested permissions
         */
        fun builder(context: Context, permissions: Array<String>) = Builder(context, permissions)

        /**
         * Constructs the builder. Convenience method to use when there is only one permission to
         * check. Enforces silent mode.
         *
         * @param context    context
         * @param permission only one permission to check.
         */
        fun builder(context: Context, permission: String) = Builder(context, arrayOf(permission))

        /**
         * Constructs the builder. This method must be used for interactive mode.
         *
         * @param activity    the parent activity
         * @param permissions array of requested permissions
         */
        fun builder(activity: AppCompatActivity, permissions: Array<String>) = Builder(activity, permissions)

        /**
         * Performs a check on whether a particular permission has been granted to the application.
         *
         * @param context context
         * @param permission permission to check
         * @return true if the application has the permission
         */
        fun check(context: Context, permission: String) =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        private fun makeFragmentTag(requestId: Int): String {
            return PermissionFlow::class.java.name + "_" + requestId
        }
    }

    private val permissionsGranted = LinkedHashSet<String>()
    private val permissionsNotGranted = LinkedHashSet<String>()

    private var started = false

    init {
        fragment?.bindPermissionFlow(this)
    }

    /**
     * Checks all permissions if they are granted and adds them to appropriate lists.
     */
    private fun checkPermissions() {
        permissions.forEach {
            (if (check(context, it)) permissionsGranted else permissionsNotGranted).add(it)
        }
    }

    /**
     * Starts the permission flow. Callbacks may be called immediately from this method, or they may
     * be called later, after the user interacts with permission dialogs. In silent mode, it is
     * guaranteed that callbacks will be run in the same thread before this method returns. Each
     * permission flow object can be started only once, starting it more times has no effect.
     */
    fun flow() {
        if (started) return
        started = true

        checkPermissions()

        if (silent || areAllGranted()) {
            finishFlow()
        } else {
            // interactive mode without all granted
            if (shouldShowRationale()) {
                showRationale(false)
            } else {
                showPermissionDialog(PHASE_BEFORE_RATIONALE)
            }
        }
    }

    /**
     * Returns true if all the permissions of this permission flow have been granted. In other
     * words, return false only if there is at least one not granted permission.
     *
     * @return true if all the permissions have been granted
     */
    private fun areAllGranted() = permissionsNotGranted.size == 0

    /**
     * Checks whether it is suggested to show a rationale for a particular permission.
     *
     * @param permission permission to check
     * @return true, if a rationale should be shown
     */
    private fun shouldShowRationaleFor(permission: String) =
            fragment!!.shouldShowRequestPermissionRationale(permission)

    /**
     * Goes through all not granted permissions and returns true if at least one of them suggests
     * showing a rationale.
     *
     * @return true if any of the non granted permissions suggests showing a rationale
     */
    private fun shouldShowRationale() = permissionsNotGranted.any { shouldShowRationaleFor(it) }

    /**
     * Shows a rationale, a dialog explaining the purpose of the requested permission. The user can
     * deny or reopen the permission dialog.
     *
     * @param explicitlyDenied whether this rationale is shown as a result of the user denying a
     *                         permission
     */
    private fun showRationale(explicitlyDenied: Boolean) = AlertDialog.Builder(context)
            .setTitle(if (explicitlyDenied) R.string.permissions_denied else R.string.permissions_needed)
            .setMessage(rationale)
            .setCancelable(false)
            .setPositiveButton(R.string.retry) { _, _ -> showPermissionDialog(PHASE_AFTER_RATIONALE) }
            .setNegativeButton(R.string.deny) { _, _ -> finishFlow() }
            .setOnCancelListener { finishFlow() }
            .show()

    /**
     * Make Android present user the system dialog to grant the permissions.
     *
     * @param phase phase of the permission flow, either PHASE_BEFORE_RATIONALE or
     *              PHASE_AFTER_RATIONALE
     */
    private fun showPermissionDialog(phase: Int) = fragment!!.requestPermissions(permissions, phase)

    /**
     * Method to be called from [PermissionHelperFragment] upon getting results from the
     * permission dialog.
     *
     * @param phase        Either PHASE_BEFORE_RATIONALE or PHASE_AFTER_RATIONALE, depending on
     *                     whether a rationale has been already shown in this permission flow.
     * @param permissions  the array of requested permissions
     * @param grantResults the array of results from the request
     */
    internal fun onRequestPermissionsResult(phase: Int, permissions: Array<String>, grantResults: IntArray) {
        permissions.forEachIndexed { index, permission ->
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.remove(permission)
                permissionsGranted.add(permission)
            }
        }

        if (areAllGranted()) {
            finishFlow()
        } else {
            // some of the permissions are not granted
            when (phase) {
                PHASE_BEFORE_RATIONALE -> if (shouldShowRationale()) {
                    showRationale(true)
                } else {
                    openSettingsIfNeeded()
                }
                PHASE_AFTER_RATIONALE -> if (shouldShowRationale()) {
                    // it means that the permission dialog was denied just for this time
                    finishFlow()
                } else {
                    // the permission dialog was denied forever
                    openSettingsIfNeeded()
                }
                PHASE_AFTER_SETTINGS ->
                    // permission remained denied forever
                    finishFlow()
            }
        }
    }

    /**
     * Calls [showSettingsDialog] if the permission flow is set so.
     */
    private fun openSettingsIfNeeded() {
        if (shouldOpenSettingsIfNeeded) {
            showSettingsDialog()
        } else {
            finishFlow()
        }
    }

    /**
     * This is supposed to be called when a permission is denied forever. Shows a dialog with
     * rationale and an offer to open settings of the app.
     */
    private fun showSettingsDialog() = AlertDialog.Builder(context)
            .setTitle(R.string.permissions_denied)
            .setMessage(context.getString(rationale) + "\n\n" + context.getString(R.string.permissions_navigate_to_settings))
            .setPositiveButton(R.string.settings) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", context.packageName, null)
                val requestId = REQUEST_CODES_BASE + REQUEST_OPEN_SETTINGS
                fragment!!.startActivityForResult(intent, requestId)
            }
            .setNegativeButton(R.string.dismiss) { _, _ -> finishFlow() }
            .setOnCancelListener { finishFlow() }
            .show()

    /**
     * [PermissionHelperFragment] delegates calls to [android.support.v4.app.Fragment.onActivityResult]
     * to this PermissionFlow object through this method.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its
     *                    setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be
     *                    attached to Intent "extras").
     * @return true, if the result has been consumed by the permission flow
     */
    @Suppress("UNUSED_PARAMETER")
    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        when (requestCode) {
            REQUEST_CODES_BASE + REQUEST_OPEN_SETTINGS -> {
                // the resultCode doesn't matter
                showPermissionDialog(PHASE_AFTER_SETTINGS)
                return true
            }
        }
        return false
    }

    /**
     * Finishes the permission flow by calling all the remaining callbacks.
     */
    private fun finishFlow() {
        callback ?: return

        // create result
        val results = HashMap<String, Boolean>()
        permissionsGranted.forEach { results.put(it, true) }
        permissionsNotGranted.forEach { results.put(it, false) }
        val result = PermissionFlowResult(results)

        // run callbacks
        if (areAllGranted()) {
            callback.onPermissionGranted(context, requestId, args, result)
        } else {
            callback.onPermissionDenied(context, requestId, args, result)
        }
    }

    /**
     * Builder class for constructing [PermissionFlow].
     */
    class Builder {

        private val context: Context
        private val activity: AppCompatActivity?
        private var requestId = DEFAULT_REQUEST_ID
        private var args: Bundle? = null
        private var permissions: Array<String>
        @StringRes private var rationale: StringResource? = null
        private var shouldOpenSettingsIfNeeded = true
        private var silent = true
        private var callback: PermissionCallback? = null

        /**
         * Constructs the builder.
         *
         * @param context     context
         * @param permissions array of requested permissions
         */
        internal constructor(context: Context, permissions: Array<String>) {
            this.activity = null
            this.context = context
            this.permissions = permissions
        }

        /**
         * Constructs the builder. This constructor must be used for interactive mode.
         *
         * @param activity    the parent activity
         * @param permissions array of requested permissions
         */
        internal constructor(activity: AppCompatActivity, permissions: Array<String>) {
            this.activity = activity
            this.context = activity
            this.permissions = permissions
        }

        /**
         * Set the request id. The default value is DEFAULT_REQUEST_ID.
         *
         * @param requestId id of the request that will be passed to the callback
         * @return the builder
         */
        fun requestId(requestId: Int): Builder {
            this.requestId = requestId
            return this
        }

        /**
         * Set the arguments. The default value is an empty Bundle.
         *
         * @param arguments a bundle that will be passed to the callback; null will be replaced by
         *                  an empty bundle
         * @return the builder
         */
        fun arguments(arguments: Bundle): Builder {
            this.args = arguments
            return this
        }

        /**
         * Whether to invoke a dialog offering to open settings with app's permissions if
         * appropriate. Default is true.
         *
         * @param offerSettingsIfNeeded offerSettingsIfNeeded
         */
        fun offerSettingsIfNeeded(offerSettingsIfNeeded: Boolean) {
            this.shouldOpenSettingsIfNeeded = offerSettingsIfNeeded
        }

        /**
         * Set the permission flow to be interactive, i.e. showing dialogs. The default is silent
         * behavior. For interactive permission flow, method [.builder] must be used.
         *
         * @param rationale an explanation to show if needed
         * @return the builder
         * @throws IllegalStateException if constructed without Activity
         */
        fun interactive(@StringRes rationale: StringResource): Builder {
            activity ?: throw IllegalStateException(Builder::class.java.simpleName + " constructed without Activity, interactive mode is not allowed.")
            this.rationale = rationale
            this.silent = false
            return this
        }

        /**
         * Set the permission flow to be silent, i.e. not showing any dialogs. This behavior is
         * default.
         *
         * @return the builder
         */
        fun silent(): Builder {
            this.rationale = null
            this.silent = true
            return this
        }

        /**
         * Set the callback.
         *
         * @param callback callback to notify of results
         * @return the builder
         */
        fun callback(callback: PermissionCallback?): Builder {
            this.callback = callback
            return this
        }

        /**
         * Convenience method for [callback].
         */
        fun callback(
                onGranted: (context: Context, requestId: Int, args: Bundle, result: PermissionFlowResult) -> Unit,
                onDenied: (context: Context, requestId: Int, args: Bundle, result: PermissionFlowResult) -> Unit
        ) = callback(object : PermissionCallback {
            override fun onPermissionGranted(context: Context, requestId: Int, args: Bundle, result: PermissionFlowResult) {
                onGranted.invoke(context, requestId, args, result)
            }

            override fun onPermissionDenied(context: Context, requestId: Int, args: Bundle, result: PermissionFlowResult) {
                onDenied.invoke(context, requestId, args, result)
            }
        })

        /**
         * Build and prepare the PermissionFlow.
         *
         * @return permission flow
         */
        fun build(): PermissionFlow {

            var fragment: PermissionHelperFragment? = null
            if (!silent && activity != null) {
                fragment = PermissionHelperFragment()
                fragment.retainInstance = true
                val manager = activity.supportFragmentManager
                manager.beginTransaction().add(fragment, makeFragmentTag(requestId)).commit()
                manager.executePendingTransactions()
            }

            return PermissionFlow(context, fragment,
                    requestId, args ?: Bundle(), permissions, rationale ?: 0,
                    shouldOpenSettingsIfNeeded, silent, callback)
        }

        /**
         * Build, prepare and start the PermissionFlow. In silent mode, it is guaranteed that
         * callbacks will be run in the same thread, before this method returns.
         */
        fun flow() {
            build().flow()
        }

    }

}
