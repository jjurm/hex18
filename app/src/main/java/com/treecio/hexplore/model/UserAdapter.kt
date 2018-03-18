package com.treecio.hexplore.model

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.squareup.picasso.Picasso
import com.treecio.hexplore.R
import com.treecio.hexplore.activities.ProfileActivity
import com.treecio.hexplore.utils.fromHexStringToByteArray
import java.util.*

const val USER_ID = "com.treecio.hexplore.MESSAGE"

class UserAdapter(private val userList:FlowQueryList<User>): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

        val user = userList[position]!!
        holder?.txtName?.text = user.name ?: "Unknown"
        holder?.txtShakeCount?.text = user.handshakeCount.toString()
        val mins = (Date().time - (user.lastHandshake?.time ?: 0L)) / 60_000
        holder?.txtDescription?.text = "Last handshake $mins mins ago"
        Picasso.get().load(user.profilePhoto).into(holder?.imgProfile)


        holder?.itemView?.setOnClickListener{
            view ->
            run {
                val intent = Intent(view.context, ProfileActivity::class.java).apply {
                    putExtra(USER_ID, userList[position]?.shortId?.fromHexStringToByteArray())
                }

                view.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.person_card, parent, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val txtName = itemView.findViewById<TextView>(R.id.person_card_name)
        val txtShakeCount = itemView.findViewById<TextView>(R.id.person_card_shake_count)
        val txtDescription = itemView.findViewById<TextView>(R.id.person_card_description)
        val imgProfile = itemView.findViewById<ImageView>(R.id.person_card_img)
    }

}
