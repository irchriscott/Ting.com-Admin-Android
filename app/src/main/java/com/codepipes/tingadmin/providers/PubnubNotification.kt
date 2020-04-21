package com.codepipes.tingadmin.providers

import android.content.Context
import com.pubnub.api.models.consumer.PNStatus
import com.codepipes.tingadmin.utils.Constants
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


class PubnubNotification (val context: Context) {

    public fun initialize() {

        val userAuthentication = UserAuthentication(context)
        val session = userAuthentication.get()

        val pubnubConfig = PNConfiguration()
        pubnubConfig.subscribeKey = Constants.PUBNUB_SUBSCRIBE_KEY
        pubnubConfig.publishKey = Constants.PUBNUB_PUBLISH_KEY
        pubnubConfig.isSecure = true

        val pubnub = PubNub(pubnubConfig)
        pubnub.subscribe().channels(listOf(session?.channel)).withPresence().execute()

        pubnub.addListener(object : SubscribeCallback() {

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}

            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}

            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {}

            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {}

            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
        })
    }
}