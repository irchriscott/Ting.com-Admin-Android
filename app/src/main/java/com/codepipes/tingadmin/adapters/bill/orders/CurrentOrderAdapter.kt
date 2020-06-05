package com.codepipes.tingadmin.adapters.bill.orders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.bill.LoadBillOrderDialog
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.models.Order
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_current_order.view.*

class CurrentOrderAdapter(

    private val orders: MutableSet<Order>,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener

) : RecyclerView.Adapter<CurrentOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentOrderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.row_current_order, parent, false)
        return CurrentOrderViewHolder(row)
    }

    override fun getItemCount(): Int = orders.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CurrentOrderViewHolder, position: Int) {
        val order = orders.toMutableList()[position]
        Picasso.get().load("${Routes.HOST_END_POINT}${order.user?.image}").into(holder.view.order_user_image)
        holder.view.order_menu_name.text = "${order.quantity} - ${order.menu.menu.name}"
        holder.view.order_table_number.text = order.tableNumber
        if(order.waiter != null) {
            Picasso.get().load("${Routes.HOST_END_POINT}${order.waiter.image}").into(holder.view.order_waiter_image)
            holder.view.order_waiter_name.text = order.waiter.name
        } else { holder.view.order_waiter_view.visibility = View.GONE }
        holder.view.order_bill_number.text = order.billNumber
        holder.view.order_people.text = order.people.toString()
        holder.view.order_has_promotion.text = if(order.hasPromotion) { "YES" } else { "NO" }

        holder.view.setOnClickListener {
            val loadBillOrderDialog = LoadBillOrderDialog()
            val bundle = Bundle()
            bundle.putString(Constants.ORDER_KEY, Gson().toJson(order))
            bundle.putInt(Constants.TYPE_KEY, 1)
            loadBillOrderDialog.arguments = bundle
            loadBillOrderDialog.setDataUpdatedListener(object : DataUpdatedListener {
                override fun onDataUpdated() {
                    loadBillOrderDialog.dismiss()
                    dataUpdatedListener.onDataUpdated()
                }
            })
            loadBillOrderDialog.show(fragmentManager, loadBillOrderDialog.tag)
        }
    }

    public fun addItems(ordersOthers : MutableList<Order>) {
        val lastPosition = orders.size
        orders.addAll(ordersOthers)
        notifyItemRangeInserted(lastPosition, ordersOthers.size)
    }
}

class CurrentOrderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}