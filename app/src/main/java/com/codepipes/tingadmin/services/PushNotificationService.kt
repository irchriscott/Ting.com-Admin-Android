package com.codepipes.tingadmin.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.providers.UserAuthentication
import com.google.gson.JsonParser
import com.codepipes.tingadmin.activities.base.TingDotCom
import com.squareup.picasso.Picasso
import java.lang.Exception


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
                val channel = pusher.subscribe(user.channel)

                channel.bind(user.channel) { event ->
                    val data = JsonParser().parse(event.data).asJsonObject

                    val title = data["title"].asString
                    val body = data["body"].asString

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val importance = NotificationManager.IMPORTANCE_HIGH
                        val notificationChannel = NotificationChannel(user.channel, title, importance)
                        notificationChannel.description = body
                        notificationChannel.enableLights(true)
                        notificationChannel.lightColor = Color.BLUE
                        notificationChannel.enableVibration(true)
                        notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                        notificationChannel.setShowBadge(true)
                        notificationManager.createNotificationChannel(notificationChannel)
                    }

                    val builder = NotificationCompat.Builder(this, user.channel)
                        .setSmallIcon(R.drawable.logo_round)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)

                    if(data.has("image") && data.get("image").asString != null) {
                        builder
                            .setLargeIcon(Picasso.get().load(data["image"].asString).get())
                            .setStyle(NotificationCompat.BigPictureStyle()
                                .bigLargeIcon(null)
                                .bigPicture(Picasso.get().load(data["image"].asString).get())
                                .setBigContentTitle(title)
                                .setSummaryText(body)
                            )
                    }

                    if(data.has("text")) {
                        if (data["text"].asString.replace("\\s", "") != "") {
                            builder.setStyle(NotificationCompat.BigTextStyle()
                                .bigText(data["text"].asString)
                            )
                        }
                    }

                    if(data.has("navigate")) {
                        when (data["navigate"].asString) { }
                    }

                    val notificationId = (0..1000000000).random()
                    notificationManager.notify(notificationId, builder.build())
                }
            } catch (e: Exception) {}
        }

        return Service.START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }
}
