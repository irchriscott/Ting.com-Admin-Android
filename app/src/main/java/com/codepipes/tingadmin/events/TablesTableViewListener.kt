package com.codepipes.tingadmin.events

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
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.dialogs.table.AssignWaiterTableDialog
import com.codepipes.tingadmin.dialogs.table.EditTableDialog
import com.codepipes.tingadmin.dialogs.table.TableQRCodeDialog
import com.codepipes.tingadmin.interfaces.*
import com.codepipes.tingadmin.models.RestaurantTable
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.models.Waiter
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.tableview.ITableView
import com.codepipes.tingadmin.tableview.listener.ITableViewListener
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TablesTableViewListener (

    private val mTableView: ITableView,
    private val tables: MutableList<RestaurantTable>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val tablesMenu = mutableMapOf<Int, String>(0 to "QR Code", 1 to "Edit", 2 to "Avail")

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showTableMenu(column, row)
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        val table = tables[row]
        if(column == 5) {
            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager, progressOverlay.tag)
            TingClient.getRequest(Routes.administratorsWaiter, null, session.token) { _, isSuccess, result ->
                activity.runOnUiThread {
                    progressOverlay.dismiss()
                    if(isSuccess) {
                        try {
                            val waiters = gson.fromJson<MutableList<Waiter>>(result, object : TypeToken<MutableList<Waiter>>(){}.type)
                            if(table.waiter != null) {
                                waiters.add(waiters.size, Waiter(0, "", "", "", "", "", "", "", "", false, "", ArrayList(), "", ""))
                            }
                            val assignWaiterTableDialog = AssignWaiterTableDialog()
                            assignWaiterTableDialog.setWaiters(waiters, object : SelectItemListener {
                                override fun onSelectItem(position: Int) {
                                    assignWaiterTableDialog.dismiss()
                                    if(position != 0) { assignWaiterToTable(position, table.id)
                                    } else { removeWaiterToTable(table.id) }
                                }
                            })
                            assignWaiterTableDialog.show(fragmentManager, assignWaiterTableDialog.tag)
                        } catch (e: java.lang.Exception) {
                            try {
                                val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                                TingToast(activity, serverResponse.message, TingToastType.ERROR).showToast(Toast.LENGTH_LONG)
                            } catch (e: java.lang.Exception) { TingToast(activity, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                        }
                    } else { TingToast(activity, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                }
            }
        } else { showTableMenu(column, row) }
    }

    override fun onColumnHeaderDoubleClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showTableMenu(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    private fun showTableMenu(column: Int, row: Int) {

        val table = tables[row]
        val menuList = mutableListOf<String>()

        tablesMenu[2] = if(table.isAvailable) { "Unavail" } else { "Avail" }

        if(session.permissions.contains("can_view_table")) {
            menuList.add(tablesMenu[0]!!)
        }
        if(session.permissions.contains("can_update_table")) {
            menuList.add(tablesMenu[1]!!)
        }

        if(session.permissions.contains("can_avail_table")) {
            menuList.add(tablesMenu[2]!!)
        }

        val tableBundle = Bundle()
        tableBundle.putString(Constants.TABLE_KEY, Gson().toJson(table))

        val actionSheet = ActionSheet(context, menuList)
            .setTitle("Options")
            .setColorData(context.resources.getColor(R.color.colorGray))
            .setColorTitleCancel(context.resources.getColor(R.color.colorGoogleRedTwo))
            .setColorSelected(context.resources.getColor(R.color.colorPrimary))
            .setCancelTitle("Cancel")

        actionSheet.create(object : ActionSheetCallBack {

            @SuppressLint("DefaultLocale")
            override fun data(data: String, position: Int) {
                when(data) {
                    tablesMenu[0] -> {
                        val tableQRCodeDialog = TableQRCodeDialog()
                        tableQRCodeDialog.arguments = tableBundle
                        tableQRCodeDialog.show(fragmentManager, tableQRCodeDialog.tag)
                    }
                    tablesMenu[1] -> {
                        val editTableDialog = EditTableDialog()
                        editTableDialog.arguments = tableBundle
                        editTableDialog.setFormDialogListener(object : FormDialogListener {
                            override fun onSave() {
                                editTableDialog.dismiss()
                                dataUpdatedListener.onDataUpdated()
                            }
                            override fun onCancel() { editTableDialog.dismiss() }
                        })
                        editTableDialog.show(fragmentManager, editTableDialog.tag)
                    }
                    tablesMenu[2] -> {
                        val confirmDialog = ConfirmDialog()
                        val bundle = Bundle()
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "${tablesMenu[2]?.capitalize()} Table")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to ${tablesMenu[2]?.toLowerCase()} this table ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()

                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)

                                TingClient.getRequest("${Routes.availTableToggle}${table.id}/", null, session.token) { _, isSuccess, result ->
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
            }
        })
    }

    private fun assignWaiterToTable(waiter: Int, table: Int) {
        val confirmDialog = ConfirmDialog()
        val bundle = Bundle()
        bundle.putString(Constants.CONFIRM_TITLE_KEY, "Assign Default Waiter To Table")
        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to assign default waiter this table ?")
        confirmDialog.arguments = bundle
        confirmDialog.onDialogListener(object : ConfirmDialogListener {
            override fun onAccept() {
                confirmDialog.dismiss()

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager, progressOverlay.tag)

                TingClient.getRequest("${Routes.assignWaiterTable}$waiter/$table/", null, session.token) { _, isSuccess, result ->
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

    private fun removeWaiterToTable(table: Int) {
        val confirmDialog = ConfirmDialog()
        val bundle = Bundle()
        bundle.putString(Constants.CONFIRM_TITLE_KEY, "Remove Default Waiter To Table")
        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to remove default waiter this table ?")
        confirmDialog.arguments = bundle
        confirmDialog.onDialogListener(object : ConfirmDialogListener {
            override fun onAccept() {
                confirmDialog.dismiss()

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager, progressOverlay.tag)

                TingClient.getRequest("${Routes.removeWaiterTable}$table/", null, session.token) { _, isSuccess, result ->
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