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
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.dialogs.promotion.EditPromotionDialog
import com.codepipes.tingadmin.dialogs.promotion.LoadPromotionDialog
import com.codepipes.tingadmin.interfaces.ActionSheetCallBack
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.MenuPromotion
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
class PromotionsTableViewListener (

    private val mTableView: ITableView,
    private val promotions: MutableList<MenuPromotion>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val promotionMenu = mutableMapOf<Int, String>(0 to "Edit", 1 to "Delete", 2 to "Switch On")

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showPromotionMenu(column, row)
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {

        val promotion = promotions[row]
        val promotionBundle = Bundle()
        promotionBundle.putString(Constants.PROMOTION_KEY, Gson().toJson(promotion))

        if(column == 7) { showPromotionMenu(column, row) }
        else {
            val loadPromotionDialog = LoadPromotionDialog()
            loadPromotionDialog.arguments = promotionBundle
            loadPromotionDialog.show(fragmentManager, loadPromotionDialog.tag)
        }
    }

    override fun onColumnHeaderDoubleClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showPromotionMenu(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    private fun showPromotionMenu(column: Int, row: Int) {

        val promotion = promotions[row]
        val menuList = mutableListOf<String>()

        if(promotion.isOn) {
            promotionMenu[2] = "Switch Off"
        } else { promotionMenu[2] = "Switch On" }

        if(session.permissions.contains("can_update_promotion")) {
            menuList.add(promotionMenu[0]!!)
        }
        if(session.permissions.contains("can_delete_promotion")) {
            menuList.add(promotionMenu[1]!!)
        }
        if(session.permissions.contains("can_avail_promotion")) {
            menuList.add(promotionMenu[2]!!)
        }

        val promotionBundle = Bundle()
        promotionBundle.putString(Constants.PROMOTION_KEY, Gson().toJson(promotion))

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
                    promotionMenu[0] -> {
                        val editPromotionDialog = EditPromotionDialog()
                        editPromotionDialog.arguments = promotionBundle
                        editPromotionDialog.show(fragmentManager, editPromotionDialog.tag)
                        editPromotionDialog.setFormDialogListener(object : FormDialogListener {
                            override fun onSave() {
                                editPromotionDialog.dismiss()
                                dataUpdatedListener.onDataUpdated()
                            }
                            override fun onCancel() { editPromotionDialog.dismiss() }
                        })
                    }
                    promotionMenu[1] -> {
                        val confirmDialog = ConfirmDialog()
                        val bundle = Bundle()
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "Delete Promotion")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to delete this promotion ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()

                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)

                                TingClient.getRequest(Routes.promotionDelete.format(promotion.id), null, session.token) { _, isSuccess, result ->
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
                    promotionMenu[2] -> {
                        val confirmDialog = ConfirmDialog()
                        val bundle = Bundle()
                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "${promotionMenu[2]}  Promotion")
                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to ${promotionMenu[2]?.toLowerCase()} this promotion ?")
                        confirmDialog.arguments = bundle
                        confirmDialog.onDialogListener(object : ConfirmDialogListener {
                            override fun onAccept() {
                                confirmDialog.dismiss()

                                val progressOverlay = ProgressOverlay()
                                progressOverlay.show(fragmentManager, progressOverlay.tag)

                                TingClient.getRequest(Routes.promotionAvailToggle.format(promotion.id), null, session.token) { _, isSuccess, result ->
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