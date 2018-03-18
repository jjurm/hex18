package com.treecio.hexplore.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.treecio.hexplore.LoginActivity
import com.treecio.hexplore.R
import com.treecio.hexplore.ble.BleConfig
import com.treecio.hexplore.ble.BleService
import com.treecio.hexplore.ble.Preferences
import com.treecio.hexplore.db.UsersReloadNeededEvent
import com.treecio.hexplore.model.User
import com.treecio.hexplore.model.UserAdapter
import com.treecio.hexplore.model.User_Table
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

        setContentView(R.layout.activity_people)
        /*supportActionBar?.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE
        supportActionBar?.setLogo(R.mipmap.ic_launcher)*/

        usersList = SQLite.select().from(User::class.java)
                .where(User_Table.name.isNotNull)
                .and(User_Table.handshakeCount.greaterThanOrEq(BleConfig.HANDSHAKE_TARGET))
                .flowQueryList()

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


        if (Preferences.getLocalOccupation(this).isNullOrEmpty()) {
            startActivity(Intent(this, BioActivity::class.java))
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_people, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_my_profile -> {
            // User chose the "Settings" item, show the app settings UI...
            startActivity(Intent(this, BioActivity::class.java))
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

}
