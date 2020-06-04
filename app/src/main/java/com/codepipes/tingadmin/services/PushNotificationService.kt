package com.codepipes.tingadmin.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.providers.UserAuthentication
import com.google.gson.JsonParser
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PusherEvent
import com.squareup.picasso.Picasso


class PushNotificationService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val userAuthentication = UserAuthentication(this@PushNotificationService)
        val user = userAuthentication.get()

        if (user != null && userAuthentication.isLoggedIn()) {
            val options = PusherOptions()

            options.setCluster("mt1")
            val pusher = Pusher("299875b04b5fe1dc527a", options)

            try {

                pusher.connect()
                val userChannel = pusher.subscribe(user.channel)
                val branchChannel = pusher.subscribe(user.branch.channel)

                userChannel.bind(user.channel) { showNotification(it) }
                branchChannel.bind(user.branch.channel) { showNotification(it) }

            } catch (e: Exception) { e.printStackTrace() }
        }

        return Service.START_STICKY
    }

    private fun showNotification(event: PusherEvent) {

        val userAuthentication = UserAuthentication(this@PushNotificationService)
        val user = userAuthentication.get()
        val data = JsonParser().parse(event.data).asJsonObject

        val title = data["title"].asString
        val body = data["body"].asString

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(user!!.channel, title, importance)
            notificationChannel.description = body
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = R.color.colorPrimary
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, user!!.channel)
            .setSmallIcon(R.drawable.logo_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSound(soundUri)

        if(data.has("image")) {
            if(data.get("image").asString != null && data.get("image").asString != ""){
                builder
                    .setLargeIcon(Picasso.get().load(data["image"].asString).get())
                    .setStyle(NotificationCompat.BigPictureStyle()
                        .bigLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo_round))
                        .bigPicture(Picasso.get().load(data["image"].asString).get())
                        .setBigContentTitle(title)
                        .setSummaryText(body)
                    )
            }
        }

        if(data.has("text")) {
            if (data["text"].asString.replace("\\s", "") != "") {
                builder.setStyle(NotificationCompat.BigTextStyle()
                    .bigText(data["text"].asString)
                )
            }
        }

        if(data.has("navigate")) {
            if(data["navigate"].asString != null && data["navigate"].asString != ""){
                when (data["navigate"].asString) { }
            }
        }

        try {
            val notification =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone.play()
        } catch (e: java.lang.Exception) { }

        val notificationId = (0..1000000000).random()
        notificationManager.notify(notificationId, builder.build())
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }
}
