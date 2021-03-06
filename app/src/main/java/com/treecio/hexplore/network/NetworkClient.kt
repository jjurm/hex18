package com.treecio.hexplore.network

import android.content.Context
import com.facebook.AccessToken
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.treecio.hexplore.ble.Preferences
import com.treecio.hexplore.db.UsersReloadNeededEvent
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.User_Table
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.IOException


class NetworkClient(val context: Context) {

    companion object {

        val JSON = MediaType.parse("application/json; charset=utf-8")

        //val BASE = "http://sq.jjurm.com:5005/api/v1"
        val BASE = "https://hex18.herokuapp.com/api/v1"
        val ENDPOINT_PROFILES = "$BASE/profiles"
        val ENDPOINT_ADDUSER = "$BASE/addUser"
        val ENDPOINT_ADDDESCRIPTION = "$BASE/addDescriptionForUser"
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
                try {
                    val obj = gson.fromJson(responseString, clazz)
                    handler.invoke(obj)
                } catch (e: JsonSyntaxException) {
                    Timber.w(e)
                }
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
                try {
                    val obj = gson.fromJson(responseString, responseClazz)
                    handler.invoke(obj)
                } catch (e: JsonSyntaxException) {
                    Timber.w(e)
                }
            }

        })
    }

    fun register(facebookToken: AccessToken, callback: () -> Unit) {
        val obj = AddUserRequest(Preferences.getDeviceIdString(context),
                facebookToken.userId, facebookToken.token)
        post(ENDPOINT_ADDUSER, obj, AddUserResponse::class.java, { response ->
            Preferences.saveLocalUserId(context, response.user_id)
            Preferences.saveLocalName(context, response.name ?: "No name")
            Preferences.saveLocalPhotoUrl(context, response.image_url
                    ?: "https://images-na.ssl-images-amazon.com/images/I/51zLZbEVSTL._SY355_.jpg")
            Preferences.saveLocalOccupation(context, response.occupation ?: "")
            Preferences.saveLocalBio(context, response.description ?: "")
            callback()
        })
    }

    fun queryUser(deviceId: String) {
        val obj = ProfilesRequest(Preferences.getLocalUserId(context), listOf(deviceId))
        post(ENDPOINT_PROFILES, obj, ProfilesResponse::class.java, { response ->
            response.profiles.forEach { profileInfo ->
                val usr = (select from User::class where User_Table.shortId.eq(deviceId)).list.first()
                usr.name = profileInfo.name
                usr.profilePhoto = profileInfo.image_url
                usr.profileUrl = profileInfo.facebook_url
                usr.occupation = profileInfo.occupation
                usr.bio = profileInfo.description
                usr.save()
                EventBus.getDefault().post(UsersReloadNeededEvent())
            }
        })
    }

    fun updateBio(userId: String, occupation: String, bio: String, callback: () -> Unit) {
        val obj = AddDescriptionForUserRequest(userId, occupation, bio)
        post(ENDPOINT_ADDDESCRIPTION, obj, AddDescriptionForUserResponse::class.java) { response ->
            callback()
        }
    }

    private class AddUserRequest(
            val device_id: String,
            val user_id: String,
            val facebook_token: String
    )

    private class AddUserResponse(
            val user_id: String,
            val name: String?,
            val image_url: String?,
            val occupation: String?,
            val description: String?
    )

    private class ProfilesRequest(
            val user_id: String,
            val ids: List<String>
    )

    private class ProfilesResponse(
            val profiles: List<ProfileData>
    ) {
        class ProfileData(
                val id: String,
                val name: String,
                val image_url: String,
                val facebook_url: String,
                val occupation: String,
                val description: String?
        )
    }

    private class AddDescriptionForUserRequest(
            val user_id: String,
            val occupation: String,
            val description: String
    )

    private class AddDescriptionForUserResponse()

}
