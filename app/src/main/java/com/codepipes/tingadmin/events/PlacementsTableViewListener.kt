package com.codepipes.tingadmin.events

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.custom.ActionSheet
import com.codepipes.tingadmin.dialogs.admin.LoadAdministratorDialog
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.dialogs.placement.LoadPlacementDialog
import com.codepipes.tingadmin.dialogs.table.AssignWaiterTableDialog
import com.codepipes.tingadmin.interfaces.ActionSheetCallBack
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.Placement
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
import java.lang.Exception
import java.util.zip.DataFormatException

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PlacementsTableViewListener (

    private val mTableView: ITableView,
    private val placements: MutableList<Placement>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val placementMenu = mutableMapOf<Int, String>(0 to "End Placement")

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showPlacementMenu(column, row)
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        val placement = placements[row]
        val placementBundle = Bundle()
        placementBundle.putString(Constants.PLACEMENT_KEY, Gson().toJson(placement))

        when (column) {
            6 -> {
                if(placement.waiter != null) {
                    val adminBundle = Bundle()
                    adminBundle.putString(Constants.ADMIN_KEY, gson.toJson(placement.waiter))
                    adminBundle.putInt("type", 1)

                    val loadAdministratorDialog = LoadAdministratorDialog()
                    loadAdministratorDialog.setType(1)
                    loadAdministratorDialog.arguments = adminBundle
                    loadAdministratorDialog.show(fragmentManager, loadAdministratorDialog.tag)
                } else {
                    if(session.permissions.contains("can_assign_table")) {
                        val progressOverlay = ProgressOverlay()
                        progressOverlay.show(fragmentManager, progressOverlay.tag)
                        TingClient.getRequest(Routes.administratorsWaiter, null, session.token) { _, isSuccess, result ->
                            activity.runOnUiThread {
                                progressOverlay.dismiss()
                                if(isSuccess) {
                                    try {
                                        val waiters = gson.fromJson<MutableList<Waiter>>(result, object : TypeToken<MutableList<Waiter>>(){}.type)
                                        val assignWaiterTableDialog = AssignWaiterTableDialog()
                                        assignWaiterTableDialog.setWaiters(waiters, object :
                                            SelectItemListener {
                                            override fun onSelectItem(position: Int) {
                                                assignWaiterTableDialog.dismiss()
                                                assignWaiterToTable(position, placement.token)
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
                    }
                }
            }
            7 -> showPlacementMenu(column, row)
            else -> {
                val loadPlacementDialog = LoadPlacementDialog()
                loadPlacementDialog.arguments = placementBundle
                loadPlacementDialog.show(fragmentManager, loadPlacementDialog.tag)
                loadPlacementDialog.setDataUpdatedListener(object : DataUpdatedListener {
                    override fun onDataUpdated() {
                        loadPlacementDialog.dismiss()
                        dataUpdatedListener.onDataUpdated()
                    }
                })
            }
        }
    }

    override fun onColumnHeaderDoubleClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showPlacementMenu(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    private fun showPlacementMenu(column: Int, row: Int) {

        val placement = placements[row]
        val menuList = mutableListOf<String>()

        if(session.permissions.contains("can_done_placement")) {
            menuList.add(placementMenu[0]!!)
        }

        val placementBundle = Bundle()
        placementBundle.putString(Constants.PLACEMENT_KEY, Gson().toJson(placement))

        if(menuList.size > 0) {

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
                        placementMenu[0] -> {
                            val confirmDialog = ConfirmDialog()
                            val bundle = Bundle()
                            bundle.putString(Constants.CONFIRM_TITLE_KEY, "End Placement")
                            bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to end this placement ?")
                            confirmDialog.arguments = bundle
                            confirmDialog.onDialogListener(object : ConfirmDialogListener {
                                override fun onAccept() {

                                    confirmDialog.dismiss()

                                    val progressOverlay = ProgressOverlay()
                                    progressOverlay.show(fragmentManager, progressOverlay.tag)

                                    TingClient.getRequest(Routes.placementEnd.format(placement.token), null, session.token) { _, isSuccess, result ->
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
    }

    private fun assignWaiterToTable(waiter: Int, token: String) {
        val confirmDialog = ConfirmDialog()
        val bundle = Bundle()
        bundle.putString(Constants.CONFIRM_TITLE_KEY, "Assign Waiter To Table")
        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to assign this waiter this table ?")
        confirmDialog.arguments = bundle
        confirmDialog.onDialogListener(object : ConfirmDialogListener {
            override fun onAccept() {
                confirmDialog.dismiss()

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager, progressOverlay.tag)

                TingClient.getRequest(Routes.placementAssignWaiter.format(token, waiter), null, session.token) { _, isSuccess, result ->
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