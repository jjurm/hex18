package com.treecio.summapp.permission

import java.util.*

/**
 * The result of a permission flow. Contains a boolean value to all requested permission indicating
 * whether the permission has been granted.
 */
class PermissionFlowResult internal constructor(private val map: HashMap<String, Boolean>) {

    /**
     * Returns true if the permission has been granted. Returns false when the permission has been
     * denied or not requested at all.

     * @param permission a permission to check
     * @return true if the permission is granted
     */
    fun check(permission: String): Boolean {
        return map[permission] ?: false
    }

    /**
     * Returns true if and only if all the given permissions have been granted. Returns false if any
     * of the given permissions has been denied or not requested at all.

     * @param permissions an array of permissions to check
     * @return true if all the permissions are granted
     */
    fun checkAll(permissions: Array<String>): Boolean {
        return permissions.all { check(it) }
    }

}
