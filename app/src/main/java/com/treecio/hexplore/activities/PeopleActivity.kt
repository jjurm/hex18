package com.treecio.hexplore.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.treecio.hexplore.LoginActivity
import com.treecio.hexplore.R
import com.treecio.hexplore.ble.BleService
import com.treecio.hexplore.db.UsersReloadNeededEvent
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.UserAdapter
import kotlinx.android.synthetic.main.activity_people.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PeopleActivity : BaseActivity() {

    private var usersList: FlowQueryList<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isLoginValid() || !hasNecessaryPermissions()) {
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        usersList = SQLite.select().from(User::class.java).flowQueryList()

        setContentView(R.layout.activity_people)

        rv.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        rv.layoutManager = llm

        val adapter = UserAdapter(usersList!!)
        rv.adapter = adapter
        usersList!!.addOnCursorRefreshListener {
            rv.adapter.notifyDataSetChanged()
        }

        val intent = Intent(this, BleService::class.java)
        intent.putExtra(BleService.EXTRA_ACTION, BleService.ACTION_START)
        startService(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UsersReloadNeededEvent) {
        usersList!!.refreshAsync() // this will trigger the OnCursorRefreshListener event
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        usersList?.close()

        val intent = Intent(this, BleService::class.java)
        intent.putExtra(BleService.EXTRA_ACTION, BleService.ACTION_STOP)
        startService(intent)
    }
}
