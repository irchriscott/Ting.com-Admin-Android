package com.codepipes.tingadmin.events

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.custom.ActionSheet
import com.codepipes.tingadmin.dialogs.admin.EditAdministratorDialog
import com.codepipes.tingadmin.dialogs.admin.LoadAdministratorDialog
import com.codepipes.tingadmin.dialogs.admin.UpdatePermissionsDialog
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.ActionSheetCallBack
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Route
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.tableview.ITableView
import com.codepipes.tingadmin.tableview.listener.ITableViewListener
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import okhttp3.Interceptor
import java.lang.Exception

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AdministratorsTableViewListener (

    private val mTableView: ITableView,
    private val administrators: MutableList<Administrator>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val administratorMenu = mutableMapOf<Int, String>(
        0 to "Load", 1 to "Edit", 2 to "Permissions", 3 to "Disable"
    )

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showAdministratorMenu(column, row)
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {

        val administrator = administrators[row]

        val adminBundle = Bundle()
        adminBundle.putString(Constants.ADMIN_KEY, gson.toJson(administrator))
        adminBundle.putInt("type", 0)

        if(column == 6) { showAdministratorMenu(column, row)
        } else {
            val loadAdministratorDialog = LoadAdministratorDialog()
            loadAdministratorDialog.setType(0)
            loadAdministratorDialog.arguments = adminBundle
            loadAdministratorDialog.show(fragmentManager, loadAdministratorDialog.tag)
        }
    }

    override fun onColumnHeaderDoubleClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showAdministratorMenu(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    private fun showAdministratorMenu(column: Int, row: Int) {

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

        val adminBundle = Bundle()
        adminBundle.putString(Constants.ADMIN_KEY, gson.toJson(administrator))
        adminBundle.putInt("type", 0)

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
                        loadAdministratorDialog.setType(0)
                        loadAdministratorDialog.arguments = adminBundle
                        loadAdministratorDialog.show(fragmentManager, loadAdministratorDialog.tag)
                    }
                    administratorMenu[1] -> {
                        val editAdministratorDialog = EditAdministratorDialog()
                        editAdministratorDialog.arguments = adminBundle
                        editAdministratorDialog.show(fragmentManager, editAdministratorDialog.tag)
                        editAdministratorDialog.setFormDialogListener(object : FormDialogListener {
                            override fun onSave() {
                                editAdministratorDialog.dismiss()
                                dataUpdatedListener.onDataUpdated()
                            }
                            override fun onCancel() { editAdministratorDialog.dismiss() }
                        })
                    }
                    administratorMenu[2] -> {
                        val updatePermissionsDialog = UpdatePermissionsDialog()
                        updatePermissionsDialog.arguments = adminBundle
                        updatePermissionsDialog.setFormDialogListener(object : FormDialogListener {
                            override fun onSave() {
                                updatePermissionsDialog.dismiss()
                                dataUpdatedListener.onDataUpdated()
                            }
                            override fun onCancel() { updatePermissionsDialog.dismiss() }
                        })
                        updatePermissionsDialog.show(fragmentManager, updatePermissionsDialog.tag)
                    }
                    administratorMenu[3] -> {
                        val confirmDialog = ConfirmDialog()
                        val bundle = Bundle()
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "${administratorMenu[3]} Administrator")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to ${administratorMenu[3]?.toLowerCase()} this administrator ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()

                                val interceptor = Interceptor {
                                    val url = it.request().url.newBuilder()
                                        .addQueryParameter("token", administrator.token)
                                        .build()
                                    val request = it.request().newBuilder()
                                        .header("Authorization", session.token)
                                        .url(url)
                                        .build()
                                    it.proceed(request)
                                }

                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)

                                TingClient.getRequest("${Routes.disableAdministratorToggle}${administrator.token}/", interceptor, session.token) { _, isSuccess, result ->
                                    activity.runOnUiThread {
                                        progressOverlay.dismiss()
                                        if(isSuccess) {
                                            try {
                                                dataUpdatedListener.onDataUpdated()
                                                val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                                                TingToast(context, serverResponse.message,
                                                    if(serverResponse.type == "success") { TingToastType.SUCCESS }
                                                    else { TingToastType.ERROR }
                                                ).showToast(Toast.LENGTH_LONG)
                                            } catch (e: Exception) { TingToast(context, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                                        } else { TingToast(context, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
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