package com.codepipes.tingadmin.events

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.branch.EditBranchDialog
import com.codepipes.tingadmin.custom.ActionSheet
import com.codepipes.tingadmin.dialogs.branch.LoadBranchDialog
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.*
import com.codepipes.tingadmin.models.Branch
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.tableview.ITableView
import com.codepipes.tingadmin.tableview.listener.ITableViewListener
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class BranchesTableViewListener (

    private val mTableView: ITableView,
    private val branches: MutableList<Branch>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val branchesMenu = mutableMapOf<Int, String>(0 to "Edit", 1 to "Avail")

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showBranchMenu(column, row)
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        val branch = branches[row]
        if (column == 8) { showBranchMenu(column, row) }
        else {
            val loadBranchDialog = LoadBranchDialog()
            val branchBundle = Bundle()
            branchBundle.putString(Constants.BRANCH_KEY, Gson().toJson(branch))
            loadBranchDialog.arguments = branchBundle
            loadBranchDialog.show(fragmentManager, loadBranchDialog.tag)
        }
    }

    override fun onColumnHeaderDoubleClicked(
        columnHeaderView: RecyclerView.ViewHolder,
        column: Int
    ) {
    }

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showBranchMenu(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    private fun showBranchMenu(column: Int, row: Int) {

        val branch = branches[row]
        val menuList = mutableListOf<String>()

        branchesMenu[1] = if (branch.isAvailable) { "Unavail" } else { "Avail" }

        if (session.permissions.contains("can_update_branch")) {
            menuList.add(branchesMenu[0]!!)
        }

        if (session.permissions.contains("can_avail_branch")) {
            menuList.add(branchesMenu[1]!!)
        }

        val branchBundle = Bundle()
        branchBundle.putString(Constants.BRANCH_KEY, Gson().toJson(branch))

        val actionSheet = ActionSheet(context, menuList)
            .setTitle("Options")
            .setColorData(context.resources.getColor(R.color.colorGray))
            .setColorTitleCancel(context.resources.getColor(R.color.colorGoogleRedTwo))
            .setColorSelected(context.resources.getColor(R.color.colorPrimary))
            .setCancelTitle("Cancel")

        actionSheet.create(object : ActionSheetCallBack {

            @SuppressLint("DefaultLocale")
            override fun data(data: String, position: Int) {
                when (data) {
                    branchesMenu[0] -> {
                        val editBranchDialog = EditBranchDialog()
                        editBranchDialog.arguments = branchBundle
                        editBranchDialog.setFormDialogListener(object : FormDialogListener {
                            override fun onSave() {
                                editBranchDialog.dismiss()
                                dataUpdatedListener.onDataUpdated()
                            }
                            override fun onCancel() { editBranchDialog.dismiss() }
                        })
                        editBranchDialog.show(fragmentManager, editBranchDialog.tag)
                    }
                    branchesMenu[1] -> {
                        val confirmDialog = ConfirmDialog()
                        val bundle = Bundle()
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "${branchesMenu[1]?.capitalize()} Branch")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to ${branchesMenu[1]?.toLowerCase()} this branch ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()

                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)

                                TingClient.getRequest("${Routes.availBranchToggle}${branch.id}/", null, session.token) { _, isSuccess, result ->
                                    activity.runOnUiThread {
                                        progressOverlay.dismiss()
                                        if (isSuccess) {
                                            try {
                                                dataUpdatedListener.onDataUpdated()
                                                val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                                                TingToast(
                                                    context, serverResponse.message,
                                                    when (serverResponse.type) {
                                                        "success" -> TingToastType.SUCCESS
                                                        "info" -> TingToastType.DEFAULT
                                                        else -> TingToastType.ERROR
                                                    }
                                                ).showToast(Toast.LENGTH_LONG)
                                            } catch (e: Exception) { TingToast(context, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                                        } else { TingToast(context, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                                    }
                                }
                            }
                            override fun onCancel() { confirmDialog.dismiss() }
                        })
                        confirmDialog.show(fragmentManager, confirmDialog.tag)
                    }
                }
            }
        })
    }
}