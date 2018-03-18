package com.treecio.hexplore.activities

import android.os.Bundle
import com.treecio.hexplore.R
import com.treecio.hexplore.ble.Preferences
import kotlinx.android.synthetic.main.activity_bio.*


class BioActivity() : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bio)

        loadContent()

        btnSave.setOnClickListener {
            Preferences.saveLocalOccupation(this, editOccupation.text.toString())
            Preferences.saveLocalBio(this, editBio.text.toString())
            finish()
        }
    }

    private fun loadContent() {

        txtName.text = Preferences.getLocalName(this) ?: "No name"
        editOccupation.setText(Preferences.getLocalOccupation(this))
        editBio.setText(Preferences.getLocalBio(this))

    }

}
