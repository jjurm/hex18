package com.treecio.hexplore.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import com.treecio.hexplore.R
import com.treecio.hexplore.activities.PeopleActivity
import com.treecio.hexplore.activities.ProfileActivity
import com.treecio.hexplore.model.USER_ID
import com.treecio.hexplore.model.User

class NotificationBuilder(private val context: Context) {

    companion object {

        @DrawableRes
        private val SMALL_ICON = R.drawable.com_facebook_button_icon // TODO replace with custom icon

        // this will be used as ID for notifications and incremented
        private var id = 1

        private fun nextId() = id++

    }

    private val notificationManager = NotificationManagerCompat.from(context)

    private fun baseBuilder() = NotificationCompat.Builder(context)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setSmallIcon(SMALL_ICON)

    private fun getUserPendingIntent(user: User, requestCode: Int): PendingIntent? {
        val resultIntent = Intent(context, ProfileActivity::class.java)
        resultIntent.putExtra(USER_ID, user.shortId?.blob)
        // Create a stack that navigates user from the target activity up through the parent stack
        // Notification --click--> ProfileActivity --back--> MainActivity --back--> Home
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(PeopleActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        // Get a PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun frequentPersonNotification(user: User) {
        val id = nextId()
        val builder = baseBuilder()
                .setContentTitle("New connection nearby")
                .setContentText("You reached 10 handshakes with ${user.name}")
                .setAutoCancel(true)
                //.setLargeIcon(<Bitmap>)
                .setContentIntent(getUserPendingIntent(user, id))
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS)
                .setCategory(Notification.CATEGORY_EMAIL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)

        notificationManager.notify(id, builder.build())
    }

}
