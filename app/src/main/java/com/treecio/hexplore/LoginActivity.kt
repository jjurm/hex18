package com.treecio.hexplore

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.treecio.hexplore.activities.PeopleActivity
import com.treecio.hexplore.network.NetworkClient
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager
    private lateinit var networkClient: NetworkClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        networkClient = NetworkClient(this)
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                networkClient.register(loginResult.accessToken) {
                    finish()
                    startActivity(Intent(this@LoginActivity, PeopleActivity::class.java))
                }
            }

            override fun onCancel() {
            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(this@LoginActivity, "There was a problem", Toast.LENGTH_SHORT).show()
            }
        })

        btn_login.setOnClickListener { LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, Arrays.asList("public_profile", "user_friends")) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
