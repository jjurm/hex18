package com.treecio.hexplore

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.treecio.hexplore.activities.PeopleActivity
import com.treecio.hexplore.network.NetworkClient
import com.treecio.hexplore.permission.PermissionCallback
import com.treecio.hexplore.permission.PermissionFlow
import com.treecio.hexplore.permission.PermissionFlowResult
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity(), PermissionCallback {

    private lateinit var callbackManager: CallbackManager
    private lateinit var networkClient: NetworkClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Remove title bar
        window.requestFeature(Window.FEATURE_ACTION_BAR);
        supportActionBar?.hide()

        setContentView(R.layout.activity_login)

        networkClient = NetworkClient(this)
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val dialog = ProgressDialog.show(this@LoginActivity, "",
                        "Logging in. Please wait...", true)
                dialog.show()

                networkClient.register(loginResult.accessToken) {
                    runOnUiThread {
                        dialog.hide()
                        finish()
                        startActivity(Intent(this@LoginActivity, PeopleActivity::class.java))
                    }
                }
            }

            override fun onCancel() {
            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(this@LoginActivity, "There was a problem", Toast.LENGTH_SHORT).show()
            }
        })

        btn_login.setOnClickListener { LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, Arrays.asList("public_profile", "user_friends")) }

        PermissionFlow.builder(this, HexploreApp.PERMISSIONS)
                .interactive(R.string.permissions_rationale)
                .callback(this)
                .flow()
    }

    override fun onPermissionGranted(context: Context, requestId: Int, args: Bundle, result: PermissionFlowResult) {
        // ok
    }

    override fun onPermissionDenied(context: Context, requestId: Int, args: Bundle, result: PermissionFlowResult) {
        Toast.makeText(this, getString(R.string.hexplore_cant_work_without_permissions), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
