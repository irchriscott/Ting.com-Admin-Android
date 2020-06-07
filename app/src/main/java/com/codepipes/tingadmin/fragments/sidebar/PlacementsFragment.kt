package com.codepipes.tingadmin.fragments.sidebar


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.placement.PlacementTableViewAdapter
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.events.PlacementsTableViewListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Placement
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import kotlinx.android.synthetic.main.fragment_placements.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception


class PlacementsFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    private lateinit var pubnub: PubNub
    private lateinit var subscribeCallback: SubscribeCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_placements, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        val pubnubConfig = PNConfiguration()
        pubnubConfig.subscribeKey = Constants.PUBNUB_SUBSCRIBE_KEY
        pubnubConfig.publishKey = Constants.PUBNUB_PUBLISH_KEY
        pubnubConfig.isSecure = true

        pubnub = PubNub(pubnubConfig)
        pubnub.subscribe().channels(listOf(session.channel, session.branch.channel)).withPresence().execute()

        subscribeCallback = object : SubscribeCallback() {

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}

            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}

            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {}

            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                activity?.runOnUiThread {
                    try {
                        val response = if(pnMessageResult.message.isJsonObject) {
                            pnMessageResult.message.asJsonObject
                        } else { Gson().fromJson(pnMessageResult.message.asString, JsonObject::class.java) }

                        when (response.get("type").asString) {
                            Constants.SOCKET_REQUEST_TABLE -> loadPlacements(view)
                            Constants.SOCKET_RESPONSE_W_TABLE -> loadPlacements(view)
                            Constants.SOCKET_REQUEST_TABLE_ORDER -> loadPlacements(view)
                            Constants.SOCKET_REQUEST_W_TABLE_ORDER -> loadPlacements(view)
                            Constants.SOCKET_REQUEST_W_NOTIFY_ORDER -> loadPlacements(view)
                            Constants.SOCKET_RESPONSE_ERROR ->
                                TingToast(
                                    activity!!,
                                    if (!response.get("message").isJsonNull) {
                                        response.get("message").asString
                                    } else { "An Error Occurred" },
                                    TingToastType.ERROR
                                ).showToast(Toast.LENGTH_LONG)

                            else -> { }
                        }
                    } catch (e: Exception) {
                        TingToast(
                            activity!!,
                            "An Error Occurred",
                            TingToastType.ERROR
                        ).showToast(Toast.LENGTH_LONG)
                    }
                }
            }

            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
        }

        pubnub.addListener(subscribeCallback)

        loadPlacements(view)

        return view
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun loadPlacements(view: View) {
        val gson = Gson()
        TingClient.getRequest(Routes.placementsAll, null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                view.progress_loader.visibility = View.GONE
                if(isSuccess) {
                    try {
                        val placements =
                            gson.fromJson<List<Placement>>(result, object : TypeToken<List<Placement>>(){}.type)

                        if(placements.isNotEmpty()){
                            view.placements_table_view.visibility = View.VISIBLE
                            view.empty_data.visibility = View.GONE

                            val placementTableViewAdapter = PlacementTableViewAdapter(context!!)
                            view.placements_table_view.adapter = placementTableViewAdapter
                            placementTableViewAdapter.setPlacementsList(placements)
                            view.placements_table_view.tableViewListener =
                                PlacementsTableViewListener(
                                    view.placements_table_view,
                                    placements.toMutableList(),
                                    context!!, fragmentManager!!,
                                    object : DataUpdatedListener {
                                        override fun onDataUpdated() { activity?.runOnUiThread { loadPlacements(view) } }
                                    }, activity!! )
                        } else {
                            view.placements_table_view.visibility = View.GONE
                            view.empty_data.visibility = View.VISIBLE

                            view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_navigation_placements))
                            view.empty_data.empty_text.text = "No Placement To Show"
                        }

                    } catch (e: Exception) {

                        view.placements_table_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE
                        view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.placements_table_view.visibility = View.GONE
                    view.empty_data.visibility = View.VISIBLE
                    view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data.empty_text.text = result.capitalize()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Bridge.clear(this)
        try { pubnub.removeListener(subscribeCallback) } catch (e: Exception){}
    }

    override fun onDetach() {
        super.onDetach()
        Bridge.clear(this)
        try { pubnub.removeListener(subscribeCallback) } catch (e: Exception){}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Bridge.clear(this)
        try { pubnub.removeListener(subscribeCallback) } catch (e: Exception){}
    }

    override fun onPause() {
        super.onPause()
        Bridge.clear(this)
        try { pubnub.removeListener(subscribeCallback) } catch (e: Exception){}
    }
}
