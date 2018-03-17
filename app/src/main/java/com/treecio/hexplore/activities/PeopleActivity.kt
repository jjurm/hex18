package com.treecio.hexplore.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.treecio.hexplore.R
import com.treecio.hexplore.ble.BleService
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.UserAdapter
import kotlinx.android.synthetic.main.activity_people.*


class PeopleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        rv.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        rv.layoutManager = llm

        val users: FlowQueryList<User> = SQLite.select().from(User::class.java).flowQueryList()

        val adapter = UserAdapter(users)
        rv.adapter = adapter

        users.close()


        val intent = Intent(this, BleService::class.java)
        intent.putExtra(BleService.EXTRA_ACTION, BleService.ACTION_START)
        startService(intent)
    }


    override fun onDestroy() {
        super.onDestroy()

        val intent = Intent(this, BleService::class.java)
        intent.putExtra(BleService.EXTRA_ACTION, BleService.ACTION_STOP)
        startService(intent)
    }
}
