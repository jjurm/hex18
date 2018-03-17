package com.treecio.hexplore.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.kotlinextensions.eq
import com.treecio.hexplore.R
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.User_Table

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

    }
}
