package com.codepipes.tingadmin.dialogs.bill

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.abstracts.EndlessScrollEventListener
import com.codepipes.tingadmin.adapters.bill.orders.CurrentOrderAdapter
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Order
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_bill_current_orders.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class LoadCurrentOrdersDialog : DialogFragment() {

    private  lateinit var session: Administrator
    private val orders: MutableList<Order> = ArrayList()

    private lateinit var ordersTimer: Timer

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_bill_current_orders, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        ordersTimer = Timer()
        session = UserAuthentication(context!!).get()!!

        ordersTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() { loadOrders(view) }
        }, TIMER_PERIOD, TIMER_PERIOD)
        loadOrders(view)

        view.refresh_current_orders.setColorSchemeColors(resources.getColor(R.color.colorPrimary), resources.getColor(R.color.colorAccentMain), resources.getColor(R.color.colorPrimaryDark), resources.getColor(R.color.colorAccentMain))
        view.refresh_current_orders.setOnRefreshListener {
            view.refresh_current_orders.isRefreshing = true
            this.loadOrders(view)
        }

        view.dialog_button_close.setOnClickListener {
            try { ordersTimer.cancel() } catch (e: Exception) {}
            dialog?.dismiss()
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

    companion object {
        private const val TIMER_PERIOD = 6000.toLong()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { ordersTimer.cancel() } catch (e: Exception) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try { ordersTimer.cancel() } catch (e: Exception) {}
    }
}