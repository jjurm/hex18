package com.treecio.hexplore.activities

import android.os.Bundle
import com.squareup.picasso.Picasso
import com.treecio.hexplore.R
import com.treecio.hexplore.ble.Preferences
import com.treecio.hexplore.network.NetworkClient
import kotlinx.android.synthetic.main.activity_bio.*


class BioActivity() : BaseActivity() {

    lateinit var networkClient: NetworkClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bio)

        networkClient = NetworkClient(this)

        loadContent()

        btnSave.setOnClickListener {
            val occupation = editOccupation.text.toString()
            val bio = editBio.text.toString()

            Preferences.saveLocalOccupation(this, occupation)
            Preferences.saveLocalBio(this, bio)

            networkClient.updateBio(Preferences.getLocalUserId(this), occupation, bio) {
                finish()
            }
            btnSave.isEnabled = false
        }
    }

    private fun loadContent() {

        val url = Preferences.getLocalPhotoUrl(this)
        if (!url.isNullOrEmpty()) {
            Picasso.get().load(url).into(imgProfile)
        }
        txtName.text = Preferences.getLocalName(this) ?: "No name"
        editOccupation.setText(Preferences.getLocalOccupation(this))
        editBio.setText(Preferences.getLocalBio(this))

    }

}
