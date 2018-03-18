package com.treecio.hexplore.ble

import android.content.Context
import android.content.SharedPreferences
import com.treecio.hexplore.R
import com.treecio.hexplore.utils.fromHexStringToByteArray
import com.treecio.hexplore.utils.toBytes
import com.treecio.hexplore.utils.toHexString
import java.util.*

object Preferences {

    private fun prefs(context: Context) = context.getSharedPreferences(
            context.getString(R.string.preferences_global), Context.MODE_PRIVATE)

    fun getDeviceIdString(context: Context): String {
        val prefs = prefs(context)
        val idString = prefs.getString(context.getString(R.string.preference_device_id), null)

        return if (idString != null) {
            idString
        } else {
            val bytes = generateAndStoreBytes(prefs, context)
            bytes.toHexString()
        }
    }

    fun getDeviceId(context: Context): ByteArray {
        val prefs = prefs(context)
        val idString = prefs.getString(context.getString(R.string.preference_device_id), null)

        return if (idString != null) {
            idString.fromHexStringToByteArray()
        } else {
            generateAndStoreBytes(prefs, context)
        }
    }

    fun saveLocalUserId(context: Context, id: String) {
        val prefs = prefs(context)
        prefs.edit().putString(context.getString(R.string.preference_local_user_id), id).apply()
    }

    fun getLocalUserId(context: Context): String {
        val prefs = prefs(context)
        return prefs.getString(context.getString(R.string.preference_local_user_id), null)
    }

    fun saveLocalName(context: Context, name: String) {
        val prefs = prefs(context)
        prefs.edit().putString(context.getString(R.string.preference_local_name), name).apply()
    }

    fun getLocalName(context: Context): String? {
        val prefs = prefs(context)
        return prefs.getString(context.getString(R.string.preference_local_name), null)
    }

    fun saveLocalPhotoUrl(context: Context, url: String) {
        val prefs = prefs(context)
        prefs.edit().putString(context.getString(R.string.preference_local_photo_url), url).apply()
    }

    fun getLocalPhotoUrl(context: Context): String? {
        val prefs = prefs(context)
        return prefs.getString(context.getString(R.string.preference_local_photo_url), null)
    }

    fun saveLocalOccupation(context: Context, occupation: String) {
        val prefs = prefs(context)
        prefs.edit().putString(context.getString(R.string.preference_local_occupation), occupation).apply()
    }

    fun getLocalOccupation(context: Context): String? {
        val prefs = prefs(context)
        return prefs.getString(context.getString(R.string.preference_local_occupation), null)
    }

    fun saveLocalBio(context: Context, bio: String) {
        val prefs = prefs(context)
        prefs.edit().putString(context.getString(R.string.preference_local_bio), bio).apply()
    }

    fun getLocalBio(context: Context): String? {
        val prefs = prefs(context)
        return prefs.getString(context.getString(R.string.preference_local_bio), null)
    }

    private fun generateAndStoreBytes(prefs: SharedPreferences, context: Context): ByteArray {
        val bytes = UUID.randomUUID().toBytes().take(BleConfig.MAX_BYTES).toByteArray()
        prefs.edit().putString(context.getString(R.string.preference_device_id), bytes.toHexString()).apply()
        return bytes
    }

}
