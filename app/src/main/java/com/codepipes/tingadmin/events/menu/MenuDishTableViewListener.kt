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
class MenuDishTableViewListener (

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
        3 to "Move To Type",
        4 to "Move To Category",
        5 to "Move To Cuisine"
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
        when (column) {
            8 -> {  }
            9 -> { showMenu(column, row) }
            else -> {
                val loadMenuDialog = LoadMenuDialog()
                loadMenuDialog.arguments = menuBundle
                loadMenuDialog.show(fragmentManager, loadMenuDialog.tag)
            }
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
            menuList.add(this.menuDataList[4]!!)
            menuList.add(this.menuDataList[5]!!)
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
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "${menuDataList[2]?.capitalize()} Menu Dish")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to ${menuDataList[2]?.toLowerCase()} this menu dish ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()
                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)
                                TingClient.getRequest("${Routes.menusDishAvailToggle}${menu.id}/", null, session.token) { _, isSuccess, result ->
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
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "Delete Menu Dish")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to delete this menu dish ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()
                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)
                                TingClient.getRequest("${Routes.menusDishDelete}${menu.id}/", null, session.token) { _, isSuccess, result ->
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
                        val types = Constants.DISH_TIME
                        val selectDialog = SelectDialog()
                        val selectBundle = Bundle()
                        selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Dish Type")
                        selectDialog.arguments = selectBundle
                        selectDialog.setItems(types.toSortedMap().map { it.value }, object :
                            SelectItemListener {
                            override fun onSelectItem(position: Int) {
                                val selectedType = position + 1
                                val confirmDialog = ConfirmDialog()
                                val bundle = Bundle()
                                bundle.putString(Constants.CONFIRM_TITLE_KEY, "Change Menu Dish Type")
                                bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to change this menu dish's type to ${types[selectedType]} ?")
                                confirmDialog.arguments = bundle
                                confirmDialog.onDialogListener(object : ConfirmDialogListener {
                                    override fun onAccept() {
                                        confirmDialog.dismiss()
                                        val progressOverlay = ProgressOverlay()
                                        progressOverlay.show(fragmentManager, progressOverlay.tag)
                                        selectDialog.dismiss()
                                        TingClient.getRequest("${Routes.menusDishMoveToType}${menu.id}/to/$selectedType/", null, session.token) { _, isSuccess, result ->
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
                    menuDataList[4] -> {
                        TingClient.getRequest(Routes.categoriesAll, null, session.token) { _, isSuccess, result ->
                            activity.runOnUiThread {
                                if (isSuccess) {
                                    try {
                                        val categories =
                                            gson.fromJson<List<FoodCategory>>(
                                                result,
                                                object : TypeToken<List<FoodCategory>>() {}.type
                                            ).sortedBy { it.id }
                                        val selectDialog = SelectDialog()
                                        val selectBundle = Bundle()
                                        selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Dish Category")
                                        selectDialog.arguments = selectBundle
                                        selectDialog.setItems(categories.map { it.name }, object :
                                            SelectItemListener {
                                            override fun onSelectItem(position: Int) {
                                                val selectedCategory = categories[position]
                                                val confirmDialog = ConfirmDialog()
                                                val bundle = Bundle()
                                                bundle.putString(Constants.CONFIRM_TITLE_KEY, "Change Menu Dish Category")
                                                bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to change this menu dish's category to ${selectedCategory.name} ?")
                                                confirmDialog.arguments = bundle
                                                confirmDialog.onDialogListener(object :
                                                    ConfirmDialogListener {
                                                    override fun onAccept() {
                                                        confirmDialog.dismiss()
                                                        val progressOverlay = ProgressOverlay()
                                                        progressOverlay.show(fragmentManager, progressOverlay.tag)
                                                        selectDialog.dismiss()
                                                        TingClient.getRequest("${Routes.menusDishMoveToCategory}${menu.id}/to/${selectedCategory.id}/", null, session.token) { _, isSuccess, result ->
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
                                    } catch (e: java.lang.Exception) { }
                                } else { TingToast(context, result, TingToastType.ERROR).showToast(
                                    Toast.LENGTH_LONG) }
                            }
                        }
                    }
                    menuDataList[5] -> {
                        val cuisines = session.branch.categories.categories.sortedBy { it.id }
                        val selectDialog = SelectDialog()
                        val selectBundle = Bundle()
                        selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Dish Cuisine")
                        selectDialog.arguments = selectBundle
                        selectDialog.setItems(cuisines.map { it.name }, object :
                            SelectItemListener {
                            override fun onSelectItem(position: Int) {
                                val selectedCuisine = cuisines[position]
                                val confirmDialog = ConfirmDialog()
                                val bundle = Bundle()
                                bundle.putString(Constants.CONFIRM_TITLE_KEY, "Change Menu Dish Cuisine")
                                bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to change this menu dish's cuisine to ${selectedCuisine.name} ?")
                                confirmDialog.arguments = bundle
                                confirmDialog.onDialogListener(object : ConfirmDialogListener {
                                    override fun onAccept() {
                                        confirmDialog.dismiss()
                                        val progressOverlay = ProgressOverlay()
                                        progressOverlay.show(fragmentManager, progressOverlay.tag)
                                        selectDialog.dismiss()
                                        TingClient.getRequest("${Routes.menusDishMoveToCuisine}${menu.id}/to/${selectedCuisine.id}/", null, session.token) { _, isSuccess, result ->
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