package com.treecio.hexplore.network

import android.content.Context
import android.os.AsyncTask
import com.facebook.AccessToken
import com.google.gson.Gson
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.treecio.hexplore.ble.Hardware
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.User_Table
import com.treecio.hexplore.utils.fromHexStringToByteArray
import okhttp3.*
import timber.log.Timber
import java.io.IOException


class NetworkClient(val context: Context) {

    companion object {

        val JSON = MediaType.parse("application/json; charset=utf-8")

        //val BASE = "http://sq.jjurm.com:5005/api/v1"
        val BASE = "https://hex18.herokuapp.com/api/v1"
        val ENDPOINT_PROFILES = "$BASE/profiles"
        val ENDPOINT_ADDUSER = "$BASE/addUser"
    }

    var client = OkHttpClient()
    val gson = Gson()

    @Throws(IOException::class)
    protected fun <T> get(url: String, clazz: Class<T>, handler: (T) -> Unit) {
        Timber.d("GET: $url")
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response) {
                val responseString = response.body()?.string()
                Timber.i("Response: $responseString")
                val obj = gson.fromJson(responseString, clazz)
                handler.invoke(obj)
            }

        })
    }

    @Throws(IOException::class)
    protected fun <T> post(url: String, data: T) {
        val json = gson.toJson(data)
        Timber.d("POST: $url")
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response) {
                val responseString = response.body()?.string()
                Timber.i("Response: $responseString")
            }

        })
    }

    @Throws(IOException::class)
    protected fun <T, R> post(url: String, data: T,
                              responseClazz: Class<R>, handler: (R) -> Unit) {
        val json = gson.toJson(data)
        Timber.d("POST: $url")
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response) {
                val responseString = response.body()?.string()
                Timber.i("Response: $responseString")
                val obj = gson.fromJson(responseString, responseClazz)
                handler.invoke(obj)
            }

        })
    }

    fun register(facebookToken: AccessToken, callback: () -> Unit) {
        val obj = AddUserRequest(Hardware.getDeviceIdString(context),
                facebookToken.userId, facebookToken.token)
        post(ENDPOINT_ADDUSER, obj, Unit::class.java, { callback() })
    }

    fun queryUser(deviceId: String) {
        val obj = ProfilesRequest(listOf(deviceId))
        post(ENDPOINT_PROFILES, obj, ProfilesResponse::class.java, { response ->
            AsyncTask.execute {
                response.profiles.entries.forEach { (deviceId, profileInfo) ->
                    val blob = Blob(deviceId.fromHexStringToByteArray())
                    val usr = (select from User::class where User_Table.shortId.eq(blob)).list.first()

                    usr.name = profileInfo.name
                    usr.save()
                }
            }
        })
    }

    private class AddUserRequest(
            val device_id: String,
            val user_id: String,
            val facebook_token: String
    )

    private class ProfilesRequest(
            val profiles: List<String>
    )

    private class ProfilesResponse(
            val profiles: Map<String, ProfileData>
    ) {
        class ProfileData(
                val name: String
        )
    }

}
