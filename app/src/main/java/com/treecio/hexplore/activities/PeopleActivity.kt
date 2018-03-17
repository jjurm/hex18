package com.treecio.hexplore.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.treecio.hexplore.R
import com.treecio.hexplore.ble.BleService
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.UserAdapter
import kotlinx.android.synthetic.main.activity_people.*


class PeopleActivity : AppCompatActivity() {

    private val usersList = SQLite.select().from(User::class.java).flowQueryList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        rv.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        rv.layoutManager = llm

        val adapter = UserAdapter(usersList)
        rv.adapter = adapter

        val intent = Intent(this, BleService::class.java)
        intent.putExtra(BleService.EXTRA_ACTION, BleService.ACTION_START)
        startService(intent)
    }


    override fun onDestroy() {
        super.onDestroy()

        usersList.close()

        val intent = Intent(this, BleService::class.java)
        intent.putExtra(BleService.EXTRA_ACTION, BleService.ACTION_STOP)
        startService(intent)
    }
}
