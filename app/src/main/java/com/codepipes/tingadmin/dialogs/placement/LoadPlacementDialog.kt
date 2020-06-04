package com.codepipes.tingadmin.dialogs.placement

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.bill.extras.BillExtraTableViewAdapter
import com.codepipes.tingadmin.adapters.bill.orders.BillOrderTableViewAdapter
import com.codepipes.tingadmin.dialogs.bill.AddBillExtraDialog
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.events.bill.BillExtrasTableViewListener
import com.codepipes.tingadmin.events.bill.BillOrdersTableViewListener
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Order
import com.codepipes.tingadmin.models.Placement
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_placement_load.view.*
import java.lang.Exception
import java.text.NumberFormat

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class LoadPlacementDialog : DialogFragment() {

    private lateinit var session: Administrator
    private var dataUpdatedListener: DataUpdatedListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_placement_load, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        session = UserAuthentication(context!!).get()!!

        val gson = Gson()
        val placement = gson.fromJson(arguments?.getString(Constants.PLACEMENT_KEY), Placement::class.java)

        Picasso.get().load(placement.user.imageURL()).into(view.place_user_image)
        view.place_user_name.text = placement.user.name
        view.place_table_number.text = placement.table.number
        view.place_number_people.text = placement.people.toString()
        view.place_bill_number.text = placement.billNumber?:"-"
        view.place_waiter_name.text = placement.waiter?.name?:"-"
        view.place_created_date.text = UtilsFunctions.formatDate(placement.createdAt)

        view.dialog_button_close.setOnClickListener { dialog?.dismiss() }

        loadPlacement(placement, view)

        view.place_end_placement.setOnClickListener {
            if (session.permissions.contains("can_done_placement")) {
                val confirmDialog = ConfirmDialog()
                val bundle = Bundle()
                bundle.putString(Constants.CONFIRM_TITLE_KEY, "End Placement")
                bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to end this placement ?")
                confirmDialog.arguments = bundle
                confirmDialog.onDialogListener(object : ConfirmDialogListener {
                    override fun onAccept() {

                        confirmDialog.dismiss()

                        val progressOverlay = ProgressOverlay()
                        progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                        TingClient.getRequest(Routes.placementEnd.format(placement.token), null, session.token) { _, isSuccess, result ->
                            activity?.runOnUiThread {
                                progressOverlay.dismiss()
                                if(isSuccess) {
                                    try {
                                        val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                                        TingToast(context!!, serverResponse.message,
                                            when (serverResponse.type) {
                                                "success" -> { TingToastType.SUCCESS }
                                                "info" -> { TingToastType.DEFAULT }
                                                else -> { TingToastType.ERROR }
                                            }
                                        ).showToast(Toast.LENGTH_LONG)
                                        if(dataUpdatedListener != null) {
                                            dataUpdatedListener?.onDataUpdated()
                                        } else { dialog?.dismiss() }
                                    } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(
                                        Toast.LENGTH_LONG) }
                                } else { TingToast(context!!, result, TingToastType.ERROR).showToast(
                                    Toast.LENGTH_LONG) }
                            }
                        }
                    }
                    override fun onCancel() {confirmDialog.dismiss() }
                })
                confirmDialog.show(fragmentManager!!, confirmDialog.tag)

            } else { TingToast(context!!, "You don't have the permission", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
        }

        view.place_mark_bill_paid.setOnClickListener {
            if (session.permissions.contains("can_done_placement")) {
                val confirmDialog = ConfirmDialog()
                val bundle = Bundle()
                bundle.putString(Constants.CONFIRM_TITLE_KEY, "Mark Bill As Paid")
                bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to mark this bill as paid ?")
                confirmDialog.arguments = bundle
                confirmDialog.onDialogListener(object : ConfirmDialogListener {
                    override fun onAccept() {

                        confirmDialog.dismiss()

                        val progressOverlay = ProgressOverlay()
                        progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                        TingClient.getRequest(Routes.placementMarkBillPaid.format(placement.id), null, session.token) { _, isSuccess, result ->
                            activity?.runOnUiThread {
                                progressOverlay.dismiss()
                                if(isSuccess) {
                                    try {
                                        val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                                        TingToast(context!!, serverResponse.message,
                                            when (serverResponse.type) {
                                                "success" -> { TingToastType.SUCCESS }
                                                "info" -> { TingToastType.DEFAULT }
                                                else -> { TingToastType.ERROR }
                                            }
                                        ).showToast(Toast.LENGTH_LONG)
                                        if(dataUpdatedListener != null) {
                                            dataUpdatedListener?.onDataUpdated()
                                        } else { dialog?.dismiss() }
                                    } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(
                                        Toast.LENGTH_LONG) }
                                } else { TingToast(context!!, result, TingToastType.ERROR).showToast(
                                    Toast.LENGTH_LONG) }
                            }
                        }
                    }
                    override fun onCancel() {confirmDialog.dismiss() }
                })
                confirmDialog.show(fragmentManager!!, confirmDialog.tag)

            } else { TingToast(context!!, "You don't have the permission", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
        }

        return view
    }

    private fun loadPlacement(placement: Placement, view: View) {
        TingClient.getRequest(Routes.placementGet.format(placement.token), null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                if(isSuccess) {
                    try {
                        val placementBill = Gson().fromJson(result, Placement::class.java)
                        view.place_content_view.visibility = View.VISIBLE
                        view.progress_loader.visibility = View.GONE
                        loadPlacementContent(placementBill, view)
                    } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                } else { TingToast(context!!, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
            }
        }
    }

    private fun loadPlacementContent(placement: Placement, view: View) {

        activity?.runOnUiThread {

            view.place_amount.text = if(placement.bill != null) {
                "${placement.bill.currency} ${NumberFormat.getNumberInstance().format(placement.bill.amount)}"
            } else { "${session.branch.restaurant?.config?.currency} 0.0" }

            view.place_discount.text = if(placement.bill != null) {
                "${placement.bill.currency} ${NumberFormat.getNumberInstance().format(placement.bill.discount)}"
            } else { "${session.branch.restaurant?.config?.currency} 0.0" }

            view.place_extras_total.text = if(placement.bill != null) {
                "${placement.bill.currency} ${NumberFormat.getNumberInstance().format(placement.bill.extrasTotal)}"
            } else { "${session.branch.restaurant?.config?.currency} 0.0" }

            view.place_tip.text = if(placement.bill != null) {
                "${placement.bill.currency} ${NumberFormat.getNumberInstance().format(placement.bill.tips)}"
            } else { "${session.branch.restaurant?.config?.currency} 0.0" }

            view.place_total.text = if(placement.bill != null) {
                "${placement.bill.currency} ${NumberFormat.getNumberInstance().format(placement.bill.total)}"
            } else { "${session.branch.restaurant?.config?.currency} 0.0" }

            val extras = if(placement.bill != null) {placement.bill.extras} else { ArrayList() }
            val currency = if(placement.bill != null) { placement.bill.currency } else { session.branch.restaurant!!.config.currency }

            if(extras.isEmpty()) {
                view.place_extras_table_view.visibility = View.GONE
            }

            val billExtraTableViewAdapter = BillExtraTableViewAdapter(context!!)
            view.place_extras_table_view.adapter = billExtraTableViewAdapter
            billExtraTableViewAdapter.setExtrasList(extras, currency)
            view.place_extras_table_view.tableViewListener =
                BillExtrasTableViewListener(
                    view.place_extras_table_view,
                    extras,
                    context!!, fragmentManager!!,
                    object : DataUpdatedListener {
                        override fun onDataUpdated() { activity?.runOnUiThread { loadPlacement(placement, view) } }
                    }, activity!! )

            if(placement.bill?.isPaid== true) { view.place_add_extra.visibility = View.GONE }

            view.place_add_extra.setOnClickListener {
                val addBillExtraDialog = AddBillExtraDialog()
                val placementBundle = Bundle()
                placementBundle.putInt(Constants.PLACEMENT_KEY, placement.id)
                addBillExtraDialog.arguments = placementBundle
                addBillExtraDialog.setFormDialogListener(object : FormDialogListener {
                    override fun onSave() {
                        addBillExtraDialog.dismiss()
                        loadPlacement(placement, view)
                    }
                    override fun onCancel() { addBillExtraDialog.dismiss() }
                })
                addBillExtraDialog.show(fragmentManager!!, addBillExtraDialog.tag)
            }

            loadPlacementBillOrders(placement, view)
        }
    }

    private fun loadPlacementBillOrders(placement: Placement, view: View) {
        TingClient.getRequest(Routes.ordersPlacementGet.format(placement.token), null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                if(isSuccess) {
                    try {
                        val typeToken = object : TypeToken<MutableList<Order>>(){}.type
                        val orders = Gson().fromJson<MutableList<Order>>(result, typeToken)
                        if(orders.isNotEmpty()) {
                            view.place_orders_table_view.visibility = View.VISIBLE
                        }
                        val billOrderTableViewAdapter = BillOrderTableViewAdapter(context!!)
                        view.place_orders_table_view.adapter = billOrderTableViewAdapter
                        billOrderTableViewAdapter.setOrdersList(orders)
                        view.place_orders_table_view.tableViewListener =
                            BillOrdersTableViewListener(
                                view.place_orders_table_view,
                                orders,
                                context!!, fragmentManager!!,
                                object : DataUpdatedListener {
                                    override fun onDataUpdated() { activity?.runOnUiThread { loadPlacement(placement, view) } }
                                }, activity!! )
                    } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                } else { TingToast(context!!, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog?.window!!.setLayout(width, height)
        }
    }

    public fun setDataUpdatedListener(listener: DataUpdatedListener) {
        dataUpdatedListener = listener
    }
}