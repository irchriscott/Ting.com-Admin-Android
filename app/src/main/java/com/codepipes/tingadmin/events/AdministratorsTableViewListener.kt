package com.codepipes.tingadmin.events

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.custom.ActionSheet
import com.codepipes.tingadmin.dialogs.admin.EditAdministratorDialog
import com.codepipes.tingadmin.dialogs.admin.LoadAdministratorDialog
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.interfaces.ActionSheetCallBack
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.tableview.ITableView
import com.codepipes.tingadmin.tableview.listener.ITableViewListener
import com.codepipes.tingadmin.utils.Constants
import com.google.gson.Gson

class AdministratorsTableViewListener (

    private val mTableView: ITableView,
    private val administrators: MutableList<Administrator>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val administratorMenu = mutableMapOf<Int, String>(
        0 to "Load", 1 to "Edit", 2 to "Permissions", 3 to "Disable"
    )

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {

        val administrator = administrators[row]
        val menuList = mutableListOf<String>()

        administratorMenu[3] = if(administrator.isDisabled) { "Enable" } else { "Disable" }

        if(session.permissions.contains("can_view_admin") || session.permissions.contains("can_view_all_admin")) {
            menuList.add(administratorMenu[0]!!)
        }
        if(session.permissions.contains("can_update_admin")) {
            menuList.add(administratorMenu[1]!!)
            menuList.add(administratorMenu[2]!!)
        }
        if(session.permissions.contains("can_disable_admin")) {
            menuList.add(administratorMenu[3]!!)
        }

        if(column == 6) {
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
                        administratorMenu[0] -> {
                            val loadAdministratorDialog = LoadAdministratorDialog()
                            val bundle = Bundle()
                            bundle.putString(Constants.ADMIN_KEY, gson.toJson(administrator))
                            loadAdministratorDialog.arguments = bundle
                            loadAdministratorDialog.show(fragmentManager, loadAdministratorDialog.tag)
                        }
                        administratorMenu[1] -> {
                            val editAdministratorDialog = EditAdministratorDialog()
                            val bundle = Bundle()
                            bundle.putString(Constants.ADMIN_KEY, gson.toJson(administrator))
                            editAdministratorDialog.arguments = bundle
                            editAdministratorDialog.show(fragmentManager, editAdministratorDialog.tag)
                            editAdministratorDialog.setFormDialogListener(object : FormDialogListener {
                                override fun onSave() {
                                    editAdministratorDialog.dismiss()
                                    dataUpdatedListener.onDataUpdated()
                                }
                                override fun onCancel() { editAdministratorDialog.dismiss() }
                            })
                        }
                        administratorMenu[2] -> {}
                        administratorMenu[3] -> {
                            val confirmDialog = ConfirmDialog()
                            val bundle = Bundle()
                            bundle.putString(Constants.CONFIRM_TITLE_KEY, "${administratorMenu[3]} Administrator")
                            bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to ${administratorMenu[3]?.toLowerCase()} this administrator ?")
                            confirmDialog.onDialogListener(object : ConfirmDialogListener {
                                override fun onAccept() {
                                    confirmDialog.dismiss()
                                    dataUpdatedListener.onDataUpdated()
                                }
                                override fun onCancel() {confirmDialog.dismiss() }
                            })
                            confirmDialog.show(fragmentManager, confirmDialog.tag)
                        }
                    }
                }
            })
        } else {
            val loadAdministratorDialog = LoadAdministratorDialog()
            val bundle = Bundle()
            bundle.putString("admin", gson.toJson(administrator))
            loadAdministratorDialog.arguments = bundle
            loadAdministratorDialog.show(fragmentManager, loadAdministratorDialog.tag)
        }
    }

    override fun onColumnHeaderDoubleClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {}

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}
}