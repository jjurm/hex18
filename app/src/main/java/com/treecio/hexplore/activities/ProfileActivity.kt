package com.treecio.hexplore.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.kotlinextensions.eq
import com.squareup.picasso.Picasso
import com.treecio.hexplore.R
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.User_Table
import android.content.Intent
import android.net.Uri


const val USER_ID = "com.treecio.hexplore.MESSAGE"

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.setHomeButtonEnabled(true)

        // Get the Intent that started this activity and extract the string

        val blob = Blob(intent.getByteArrayExtra(USER_ID))

        val user = (select from User::class where (User_Table.shortId eq blob)).flowQueryList().first()

        // Capture the layout's TextView and set the string as its text
        findViewById<TextView>(R.id.txt_profile_name).apply {
            text = user.name
        }
        findViewById<TextView>(R.id.txt_profile_shake_count).apply {
            text = user.handshakeCount.toString()
        }

        val imgProfile = findViewById<ImageView>(R.id.img_profile)
        Picasso.get().load(user.profilePhoto).into(imgProfile)

        findViewById<Button>(R.id.btn_fb_add).setOnClickListener({
            _ -> this.startActivity(getOpenFacebookIntent(user?.profileUrl, this))
        })

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
}
