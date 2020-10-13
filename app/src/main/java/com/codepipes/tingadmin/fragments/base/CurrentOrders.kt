package com.codepipes.tingadmin.fragments.base


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.abstracts.EndlessScrollEventListener
import com.codepipes.tingadmin.adapters.bill.orders.CurrentOrderAdapter
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Order
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
import kotlinx.android.synthetic.main.fragment_current_orders.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class CurrentOrders : Fragment() {

    private  lateinit var session: Administrator
    private val orders: MutableList<Order> = ArrayList()

    private lateinit var pubnub: PubNub
    private lateinit var ordersTimer: Timer

    private lateinit var subscribeCallback: SubscribeCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_current_orders, container, false)

        Bridge.clear(this)
        savedInstanceState?.clear()

        ordersTimer = Timer()
        session = UserAuthentication(context!!).get()!!

        ordersTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() { loadOrders(view) }
        }, TIMER_PERIOD, TIMER_PERIOD)
        loadOrders(view)

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
                    try { loadOrders(view)
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

        view.refresh_current_orders.setColorSchemeColors(resources.getColor(R.color.colorPrimary), resources.getColor(R.color.colorAccentMain), resources.getColor(R.color.colorPrimaryDark), resources.getColor(R.color.colorAccentMain))
        view.refresh_current_orders.setOnRefreshListener {
            view.refresh_current_orders.isRefreshing = true
            this.loadOrders(view)
        }

        return view
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun loadOrders(view: View) {
        val gson = Gson()
        TingClient.getRequest(Routes.ordersAll, null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                ordersTimer.cancel()
                view.progress_loader.visibility = View.GONE
                if(isSuccess) {
                    try {
                        val ordersResult =
                            gson.fromJson<List<Order>>(result, object : TypeToken<List<Order>>(){}.type)

                        orders.addAll(ordersResult)
                        orders.toSet().toMutableList()

                        if(orders.isNotEmpty()){
                            view.orders_recycler_view.visibility = View.VISIBLE
                            view.empty_data.visibility = View.GONE

                            view.refresh_current_orders.isRefreshing = false

                            val linearLayoutManager = LinearLayoutManager(context)
                            val currentOrderAdapter = CurrentOrderAdapter(
                                orders.distinctBy { it.id }.toMutableSet(),
                                fragmentManager!!,
                                object : DataUpdatedListener {
                                    override fun onDataUpdated() { loadOrders(view) }
                                }
                            )
                            view.orders_recycler_view.layoutManager = linearLayoutManager
                            view.orders_recycler_view.adapter = currentOrderAdapter

                            ViewCompat.setNestedScrollingEnabled(view.orders_recycler_view, false)

                            val endlessScrollEventListener = object: EndlessScrollEventListener(linearLayoutManager) {
                                override fun onLoadMore(pageNum: Int, recyclerView: RecyclerView?) {
                                    val urlPage = "${Routes.ordersAll}?page=${pageNum + 1}"
                                    TingClient.getRequest(urlPage, null, session.token) { _, isSuccess, result ->
                                        activity?.runOnUiThread {
                                            if (isSuccess) {
                                                try {
                                                    val ordersResultPage =
                                                        Gson().fromJson<MutableList<Order>>(
                                                            result,
                                                            object :
                                                                TypeToken<MutableList<Order>>() {}.type
                                                        )

                                                    orders.addAll(ordersResultPage)
                                                    orders.distinctBy { it.id }.toMutableList()
                                                    currentOrderAdapter.addItems(ordersResultPage)
                                                } catch (e: Exception) { }
                                            }
                                        }
                                    }
                                }
                            }

                            view.orders_recycler_view.addOnScrollListener(endlessScrollEventListener)

                        } else {
                            view.orders_recycler_view.visibility = View.GONE
                            view.empty_data.visibility = View.VISIBLE

                            view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_navigation_menus))
                            view.empty_data.empty_text.text = "No Order To Show"
                        }

                    } catch (e: Exception) {

                        view.orders_recycler_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE
                        view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.orders_recycler_view.visibility = View.GONE
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
        try { ordersTimer.cancel() } catch (e: Exception) {}
        try { pubnub.removeListener(subscribeCallback) } catch (e: Exception){}
    }

    override fun onDetach() {
        super.onDetach()
        Bridge.clear(this)
        try { ordersTimer.cancel() } catch (e: Exception) {}
        try { pubnub.removeListener(subscribeCallback) } catch (e: Exception){}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Bridge.clear(this)
        try { ordersTimer.cancel() } catch (e: Exception) {}
        try { pubnub.removeListener(subscribeCallback) } catch (e: Exception){}
    }

    override fun onPause() {
        super.onPause()
        Bridge.clear(this)
        try { ordersTimer.cancel() } catch (e: Exception) {}
        try { pubnub.removeListener(subscribeCallback) } catch (e: Exception){}
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    companion object {
        private const val TIMER_PERIOD = 6000.toLong()
    }
}
