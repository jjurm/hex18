package com.treecio.summapp.permission

import android.content.Context
import android.os.Bundle

/**
 * A simple interface able to receive callbacks regarding permission flow.
 */
interface PermissionCallback {

    /**
     * Called when all of the requested permissions were granted. In this case, all checks on the
     * result return true.
     *
     * @param context   context
     * @param requestId the original request id
     * @param args      additional arguments supplied to the permission flow
     * @param result    the result of the permission flow
     */
    fun onPermissionGranted(context: Context, requestId: Int, args: Bundle, result: PermissionFlowResult)

    /**
     * Called when at least one of the requested permissions has been denied.
     *
     * @param context   context
     * @param requestId the original request id
     * @param args      additional arguments supplied to the permission flow
     * @param result    the result of the permission flow
     */
    fun onPermissionDenied(context: Context, requestId: Int, args: Bundle, result: PermissionFlowResult)

}
