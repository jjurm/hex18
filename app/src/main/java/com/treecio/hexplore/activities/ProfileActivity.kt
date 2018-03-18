package com.treecio.hexplore.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.raizlabs.android.dbflow.kotlinextensions.eq
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.squareup.picasso.Picasso
import com.treecio.hexplore.R
import com.treecio.hexplore.ble.BleConfig
import com.treecio.hexplore.db.UsersReloadNeededEvent
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.User_Table
import com.treecio.hexplore.utils.toHexString
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.LocalDateTime
import java.util.*
import kotlin.concurrent.timerTask

const val USER_ID = "com.treecio.hexplore.MESSAGE"

class ProfileActivity : AppCompatActivity() {

    lateinit var user:User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get the Intent that started this activity and extract the string

        val deviceId = intent.getByteArrayExtra(USER_ID).toHexString()

        user = (select from User::class where (User_Table.shortId eq deviceId)).flowQueryList().first()

        // Capture the layout's TextView and set the string as its text
        findViewById<TextView>(R.id.txt_profile_name).apply {
            text = user.name
        }
        findViewById<TextView>(R.id.txt_profile_shake_count).apply {
            text = user.handshakeCount.toString() + " handshakes so far"
        }

        /*findViewById<TextView>(R.id.txt_profile_occupation).apply {
            text = user.occupation
        }

        findViewById<TextView>(R.id.txt_profile_bio).apply {
            text = user.bio
        }*/

        val imgProfile = findViewById<ImageView>(R.id.img_profile)
        Picasso.get().load(user.profilePhoto).into(imgProfile)

        findViewById<Button>(R.id.btn_fb_add).setOnClickListener({
            _ -> this.startActivity(getOpenFacebookIntent(user?.profileUrl, this))
        })

        val timer = Timer()
        timer.schedule(timerTask {
            user.load();
            updateNearby()
        }, 0L, BleConfig.NEARBY_THRESHOLD*1000L)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
    }

    fun updateNearby() {
        if(LocalDateTime(user.lastHandshake).isBefore(LocalDateTime.now().minusSeconds(BleConfig.NEARBY_THRESHOLD))) {
            runOnUiThread { findViewById<ConstraintLayout>(R.id.l_profile_nearby).visibility = View.GONE }
        } else {
            runOnUiThread { findViewById<ConstraintLayout>(R.id.l_profile_nearby).visibility = View.VISIBLE }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UsersReloadNeededEvent) {
        updateNearby()
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    fun getOpenFacebookIntent(url:String?, context: Context): Intent {

        try {
            //Trys to make intent with FB's URI
            context.getPackageManager()
                    .getPackageInfo("com.facebook.katana", 0) //Checks if FB is even installed.
            return Intent(Intent.ACTION_VIEW,
                    Uri.parse("fb://facewebmodal/f?href=" + url))
        } catch (e: Exception) {
            return Intent(Intent.ACTION_VIEW,
                    Uri.parse(url)) //catches and opens a url to the desired page
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        android.R.id.home
            this.finish()
            return true
    }
}
