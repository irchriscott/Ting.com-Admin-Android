package com.codepipes.tingadmin.events.menu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.custom.ActionSheet
import com.codepipes.tingadmin.dialogs.menu.EditMenuDialog
import com.codepipes.tingadmin.dialogs.menu.LoadMenuDialog
import com.codepipes.tingadmin.dialogs.messages.*
import com.codepipes.tingadmin.interfaces.*
import com.codepipes.tingadmin.models.FoodCategory
import com.codepipes.tingadmin.models.Menu
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.tableview.ITableView
import com.codepipes.tingadmin.tableview.listener.ITableViewListener
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MenuDrinkTableViewListener (

    private val mTableView: ITableView,
    private val menus: MutableList<Menu>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val menuDataList = mutableMapOf<Int, String>(
        0 to "Edit",
        1 to "Avail",
        2 to "Delete",
        3 to "Move To Type"
    )

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showMenu(column, row)
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        val menu = menus[row]
        val menuBundle = Bundle()
        menuBundle.putString(Constants.MENU_KEY, Gson().toJson(menu))
        if(column == 6) { showMenu(column, row) }
        else {
            val loadMenuDialog = LoadMenuDialog()
            loadMenuDialog.arguments = menuBundle
            loadMenuDialog.show(fragmentManager, loadMenuDialog.tag)
        }
    }

    override fun onColumnHeaderDoubleClicked(
        columnHeaderView: RecyclerView.ViewHolder,
        column: Int
    ) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showMenu(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    private fun showMenu(column: Int, row: Int) {

        val menu = menus[row]
        val menuList = mutableListOf<String>()

        this.menuDataList[1] = if (menu.isAvailable) { "Unavail" } else { "Avail" }

        if (session.permissions.contains("can_update_menu")) {
            menuList.add(this.menuDataList[0]!!)
        }

        if (session.permissions.contains("can_avail_menu")) {
            menuList.add(this.menuDataList[1]!!)
        }

        if (session.permissions.contains("can_delete_menu")) {
            menuList.add(this.menuDataList[2]!!)
        }

        if (session.permissions.contains("can_update_menu")) {
            menuList.add(this.menuDataList[3]!!)
        }

        val menuBundle = Bundle()
        menuBundle.putString(Constants.MENU_KEY, Gson().toJson(menu))

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
                    menuDataList[0] -> {
                        val editMenuDialog = EditMenuDialog()
                        editMenuDialog.arguments = menuBundle
                        editMenuDialog.show(fragmentManager, editMenuDialog.tag)
                        editMenuDialog.setFormDialogListener(object : FormDialogListener {
                            override fun onSave() {
                                editMenuDialog.dismiss()
                                dataUpdatedListener.onDataUpdated()
                            }
                            override fun onCancel() { editMenuDialog.dismiss() }
                        })
                    }
                    menuDataList[1] -> {
                        val confirmDialog = ConfirmDialog()
                        val bundle = Bundle()
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "${menuDataList[2]?.capitalize()} Menu Drink")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to ${menuDataList[2]?.toLowerCase()} this menu drink ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()
                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)
                                TingClient.getRequest("${Routes.menusDrinkAvailToggle}${menu.id}/", null, session.token) { _, isSuccess, result ->
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
                    menuDataList[2] -> {
                        val confirmDialog = ConfirmDialog()
                        val bundle = Bundle()
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "Delete Menu Drink")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to delete this menu drink ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()
                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)
                                TingClient.getRequest("${Routes.menusDrinkDelete}${menu.id}/", null, session.token) { _, isSuccess, result ->
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
                    menuDataList[3] -> {
                        val types = Constants.DRINK_TYPE
                        val selectDialog = SelectDialog()
                        val selectBundle = Bundle()
                        selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Drink Type")
                        selectDialog.arguments = selectBundle
                        selectDialog.setItems(types.toSortedMap().map { it.value }, object :
                            SelectItemListener {
                            override fun onSelectItem(position: Int) {
                                val selectedType = position + 1
                                val confirmDialog = ConfirmDialog()
                                val bundle = Bundle()
                                bundle.putString(Constants.CONFIRM_TITLE_KEY, "Change Menu Drink Type")
                                bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to change this menu drink's type to ${types[selectedType]} ?")
                                confirmDialog.arguments = bundle
                                confirmDialog.onDialogListener(object : ConfirmDialogListener {
                                    override fun onAccept() {
                                        confirmDialog.dismiss()
                                        val progressOverlay = ProgressOverlay()
                                        progressOverlay.show(fragmentManager, progressOverlay.tag)
                                        selectDialog.dismiss()
                                        TingClient.getRequest("${Routes.menusDrinkMoveToType}${menu.id}/to/$selectedType/", null, session.token) { _, isSuccess, result ->
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
                                    override fun onCancel() { confirmDialog.dismiss() }
                                })
                                confirmDialog.show(fragmentManager, confirmDialog.tag)
                            }
                        })
                        selectDialog.show(fragmentManager, selectDialog.tag)
                    }
                }
            }
        })
    }
}