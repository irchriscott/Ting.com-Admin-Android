package com.codepipes.tingadmin.events.bill

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.BillExtra
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
class BillExtrasTableViewListener (

    private val mTableView: ITableView,
    private val extras: MutableList<BillExtra>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        val billExtra = extras[row]
        if(column == 5) {
            val confirmDialog = ConfirmDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Delete Bill Extra")
            bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to delete this bill extra ?")
            confirmDialog.arguments = bundle
            confirmDialog.onDialogListener(object : ConfirmDialogListener {
                override fun onAccept() {
                    confirmDialog.dismiss()

                    val progressOverlay = ProgressOverlay()
                    progressOverlay.show(fragmentManager, progressOverlay.tag)

                    TingClient.getRequest(Routes.ordersDeleteExtra.format(billExtra.id), null, session.token) { _, isSuccess, result ->
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
    }

    override fun onColumnHeaderDoubleClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
}