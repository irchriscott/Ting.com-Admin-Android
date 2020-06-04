package com.codepipes.tingadmin.events.bill

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.custom.ActionSheet
import com.codepipes.tingadmin.dialogs.bill.DeclineBillOrderDialog
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.ActionSheetCallBack
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Order
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.tableview.ITableView
import com.codepipes.tingadmin.tableview.listener.ITableViewListener
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import java.lang.Exception

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class BillOrdersTableViewListener (

    private val mTableView: ITableView,
    private val orders: MutableList<Order>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val orderMenu = mutableMapOf<Int, String>(0 to "Accept", 1 to "Decline")

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        val order = orders[row]
        if(column == 7) {
            if(!order.isDeclined && !order.isDelivered) {
                val actionSheet = ActionSheet(context, orderMenu.toSortedMap().map { it.value }.toMutableList())
                    .setTitle("Options")
                    .setColorData(context.resources.getColor(R.color.colorGray))
                    .setColorTitleCancel(context.resources.getColor(R.color.colorGoogleRedTwo))
                    .setColorSelected(context.resources.getColor(R.color.colorPrimary))
                    .setCancelTitle("Cancel")

                actionSheet.create(object : ActionSheetCallBack {

                    @SuppressLint("DefaultLocale")
                    override fun data(data: String, position: Int) {
                        when(data) {
                            orderMenu[0] -> {
                                val confirmDialog = ConfirmDialog()
                                val bundle = Bundle()
                                bundle.putString(Constants.CONFIRM_TITLE_KEY, "Accept Order")
                                bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to accept this order ?")
                                confirmDialog.arguments = bundle
                                confirmDialog.onDialogListener(object : ConfirmDialogListener {
                                    override fun onAccept() {
                                        confirmDialog.dismiss()

                                        val progressOverlay = ProgressOverlay()
                                        progressOverlay.show(fragmentManager, progressOverlay.tag)

                                        TingClient.getRequest(Routes.ordersAccept.format(order.id), null, session.token) { _, isSuccess, result ->
                                            activity.runOnUiThread {
                                                progressOverlay.dismiss()
                                                if(isSuccess) {
                                                    try {
                                                        dataUpdatedListener.onDataUpdated()
                                                        val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                                                        TingToast(context, serverResponse.message,
                                                            when (serverResponse.type) {
                                                                "success" -> { TingToastType.SUCCESS }
                                                                "info" -> { TingToastType.DEFAULT }
                                                                else -> { TingToastType.ERROR }
                                                            }
                                                        ).showToast(Toast.LENGTH_LONG)
                                                    } catch (e: Exception) { TingToast(context, e.localizedMessage, TingToastType.ERROR).showToast(
                                                        Toast.LENGTH_LONG) }
                                                } else { TingToast(context, result, TingToastType.ERROR).showToast(
                                                    Toast.LENGTH_LONG) }
                                            }
                                        }
                                    }
                                    override fun onCancel() {confirmDialog.dismiss() }
                                })
                                confirmDialog.show(fragmentManager, confirmDialog.tag)
                            }
                            orderMenu[1] -> {
                                val declineBillOrderDialog = DeclineBillOrderDialog()
                                val orderBundle = Bundle()
                                orderBundle.putInt(Constants.ORDER_KEY, order.id)
                                declineBillOrderDialog.arguments = orderBundle
                                declineBillOrderDialog.setFormDialogListener(object : FormDialogListener {
                                    override fun onSave() {
                                        declineBillOrderDialog.dismiss()
                                        dataUpdatedListener.onDataUpdated()
                                    }
                                    override fun onCancel() { declineBillOrderDialog.dismiss() }
                                })
                                declineBillOrderDialog.show(fragmentManager, declineBillOrderDialog.tag)
                            }
                        }
                    }
                })
            }
        } else {
            val orderBundle = Bundle()
            orderBundle.putString(Constants.ORDER_KEY, gson.toJson(order))
        }
    }

    override fun onColumnHeaderDoubleClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
}