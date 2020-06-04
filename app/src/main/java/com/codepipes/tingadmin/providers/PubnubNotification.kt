package com.codepipes.tingadmin.providers

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import com.codepipes.tingadmin.custom.Noty
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.pubnub.api.models.consumer.PNStatus
import com.codepipes.tingadmin.utils.Constants
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.PubNub
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.PNConfiguration
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult


class PubnubNotification (private val activity: Activity, private val viewGroup: ViewGroup) {

    public fun initialize() {

        val userAuthentication = UserAuthentication(activity)
        val session = userAuthentication.get()!!

        val pubnubConfig = PNConfiguration()
        pubnubConfig.subscribeKey = Constants.PUBNUB_SUBSCRIBE_KEY
        pubnubConfig.publishKey = Constants.PUBNUB_PUBLISH_KEY
        pubnubConfig.isSecure = true

        val pubnub = PubNub(pubnubConfig)
        pubnub.subscribe().channels(listOf(session.channel, session.branch.channel)).withPresence().execute()

        pubnub.addListener(object : SubscribeCallback() {

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}

            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}

            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {}

            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                activity.runOnUiThread {
                    try {
                        val response = if(pnMessageResult.message.isJsonObject) {
                            pnMessageResult.message.asJsonObject
                        } else { Gson().fromJson(pnMessageResult.message.asString, JsonObject::class.java) }

                        when (response.get("type").asString) {
                            Constants.SOCKET_REQUEST_TABLE -> {
                                if(session.permissions.contains("can_assign_table")) {
                                    val sender = response.get("sender").asJsonObject
                                    val data = response.get("data").asJsonObject
                                    val message = "A new client has been placed on table ${data.get("table").asString}"
                                    Noty.init(activity, sender.get("image").asString, "New Client Placed", message, viewGroup, Noty.NotyStyle.SIMPLE)
                                        .tapToDismiss(true)
                                        .show()
                                }
                            }
                            Constants.SOCKET_REQUEST_ASSIGN_WAITER -> {
                                if(session.permissions.contains("can_assign_table")) {
                                    val sender = response.get("sender").asJsonObject
                                    val data = response.get("data").asJsonObject
                                    val message = "A waiter is needed on table ${data.get("table").asString}. Please assign ! "
                                    Noty.init(activity, sender.get("image").asString, "Waiter Needed", message, viewGroup, Noty.NotyStyle.SIMPLE)
                                        .tapToDismiss(true)
                                        .show()
                                }
                            }
                            Constants.SOCKET_RESPONSE_W_TABLE -> {
                                val data = response.get("data").asJsonObject
                                val message = "You have been assigned to the client on table ${data.get("table").asString} "
                                Noty.init(activity, data.get("user").asJsonObject.get("image").asString, "New Table For You", message, viewGroup, Noty.NotyStyle.SIMPLE)
                                    .tapToDismiss(true)
                                    .show()
                            }
                            Constants.SOCKET_REQUEST_TABLE_ORDER -> {
                                if(session.permissions.contains("can_receive_orders")) {
                                    val data = response.get("data").asJsonObject
                                    val title = "New Order On Table ${data.get("table").asString}"
                                    val message = "${data.get("user").asJsonObject.get("name").asString} has placed an order on table ${data.get("table").asString} "
                                    Noty.init(activity, data.get("user").asJsonObject.get("image").asString, title, message, viewGroup, Noty.NotyStyle.SIMPLE)
                                        .tapToDismiss(true)
                                        .show()
                                }
                            }
                            Constants.SOCKET_REQUEST_W_TABLE_ORDER -> {
                                if(!session.permissions.contains("can_receive_orders") && session.type.toInt() == 4) {
                                    val data = response.get("data").asJsonObject
                                    val title = "New Order On Table ${data.get("table").asString}"
                                    val message = "${data.get("user").asJsonObject.get("name").asString} has placed an order on table ${data.get("table").asString} "
                                    Noty.init(activity, data.get("user").asJsonObject.get("image").asString, title, message, viewGroup, Noty.NotyStyle.SIMPLE)
                                        .tapToDismiss(true)
                                        .show()
                                }
                            }
                            Constants.SOCKET_REQUEST_NOTIFY_ORDER -> {
                                val data = response.get("data").asJsonObject
                                val title = "Order Delayed On Table ${data.get("table").asString}"
                                val message = "${response.get("sender").asJsonObject.get("name").asString} has placed an order on table ${data.get("table").asString} but it seems it is delaying. Please, Accept or Decline the order."
                                Noty.init(activity, response.get("sender").asJsonObject.get("image").asString, title, message, viewGroup, Noty.NotyStyle.SIMPLE)
                                    .tapToDismiss(true)
                                    .show()
                            }
                            Constants.SOCKET_REQUEST_W_NOTIFY_ORDER -> {
                                if(!session.permissions.contains("can_receive_orders") && session.type.toInt() == 4) {
                                    val data = response.get("data").asJsonObject
                                    val title = "Order Delayed On Table ${data.get("table").asString}"
                                    val message = "${response.get("sender").asJsonObject.get("name").asString} has placed an order on table ${data.get("table").asString} but it seems it is delaying. Please, Accept or Decline the order."
                                    Noty.init(activity, response.get("sender").asJsonObject.get("image").asString, title, message, viewGroup, Noty.NotyStyle.SIMPLE)
                                        .tapToDismiss(true)
                                        .show()
                                }
                            }
                            Constants.SOCKET_RESPONSE_ERROR -> {
                                TingToast(
                                    activity,
                                    if (!response.get("message").isJsonNull) {
                                        response.get("message").asString
                                    } else { "An Error Occurred" },
                                    TingToastType.ERROR
                                ).showToast(Toast.LENGTH_LONG)
                            }
                            else -> { }
                        }
                    } catch (e: Exception) {
                        TingToast(
                            activity,
                           "An Error Occurred",
                            TingToastType.ERROR
                        ).showToast(Toast.LENGTH_LONG)
                    }
                }
            }

            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
        })
    }

    companion object {
        public fun getInstance(activity: Activity, viewGroup: ViewGroup) : PubnubNotification {
            return PubnubNotification(activity, viewGroup)
        }
    }
}