package com.codepipes.tingadmin.dialogs.bill

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Order
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_bill_load_order.view.*
import java.lang.Exception
import java.text.NumberFormat

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class LoadBillOrderDialog : DialogFragment() {

    private var dataUpdatedListener: DataUpdatedListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_bill_load_order, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()!!

        val order = Gson().fromJson(arguments?.getString(Constants.ORDER_KEY), Order::class.java)
        val fromType = arguments?.getInt(Constants.TYPE_KEY, 0)

        if(fromType == 0) {
            view.order_status_view.visibility = View.GONE
        }

        val typeText = when(order.menu.type.id) {
            1, 3 -> "Packs, Pieces"
            2 -> "Bottles, Cups"
            else -> ""
        }

        Picasso.get().load("${Routes.HOST_END_POINT}${order.menu.menu.images.images[0].image}").into(view.order_menu_image)
        view.order_menu_name.text = order.menu.menu.name
        view.order_menu_price.text = if(order.menu.menu.quantity != 1) {
            "${order.currency} ${NumberFormat.getNumberInstance().format(order.price)} / ${order.menu.menu.quantity} $typeText"
        } else { "${order.currency} ${NumberFormat.getNumberInstance().format(order.price)}" }
        view.order_quantity.text = "${order.quantity} $typeText"
        view.order_conditions.text = if(order.conditions?.replace("\\s", "") != "") {
            order.conditions
        } else { "-" }
        view.order_has_promotion.text = if(order.hasPromotion) { "YES" } else { "NO" }
        view.order_created_date.text = UtilsFunctions.formatDate(order.createdAt)
        when(order.menu.type.id) {
            1 -> view.order_menu_type_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_spoon_gray))
            2 -> view.order_menu_type_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_glass_gray))
            3 -> view.order_menu_type_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_navigation_menus))
        }

        view.dialog_button_close.setOnClickListener { dialog?.dismiss() }

        view.order_decline_button.setOnClickListener {
            val declineBillOrderDialog = DeclineBillOrderDialog()
            val orderBundle = Bundle()
            orderBundle.putInt(Constants.ORDER_KEY, order.id)
            declineBillOrderDialog.arguments = orderBundle
            declineBillOrderDialog.setFormDialogListener(object :
                FormDialogListener {
                override fun onSave() {
                    declineBillOrderDialog.dismiss()
                    if(dataUpdatedListener != null) { dataUpdatedListener?.onDataUpdated()
                    } else {
                        TingToast(context!!, "State Changed. Please, Try Again", TingToastType.DEFAULT).showToast(
                            Toast.LENGTH_LONG)
                        dialog?.dismiss()
                    }
                }
                override fun onCancel() { declineBillOrderDialog.dismiss() }
            })
            declineBillOrderDialog.show(fragmentManager!!, declineBillOrderDialog.tag)
        }

        view.order_accept_button.setOnClickListener {
            val confirmDialog = ConfirmDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Accept Order")
            bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to accept this order ?")
            confirmDialog.arguments = bundle
            confirmDialog.onDialogListener(object : ConfirmDialogListener {
                override fun onAccept() {
                    confirmDialog.dismiss()

                    val progressOverlay = ProgressOverlay()
                    progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                    TingClient.getRequest(Routes.ordersAccept.format(order.id), null, session.token) { _, isSuccess, result ->
                        activity?.runOnUiThread {
                            progressOverlay.dismiss()
                            if(isSuccess) {
                                try {
                                    val serverResponse = Gson().fromJson(result, ServerResponse::class.java)
                                    TingToast(context!!, serverResponse.message,
                                        when (serverResponse.type) {
                                            "success" -> { TingToastType.SUCCESS }
                                            "info" -> { TingToastType.DEFAULT }
                                            else -> { TingToastType.ERROR }
                                        }
                                    ).showToast(Toast.LENGTH_LONG)
                                    if(serverResponse.type == "success") {
                                        if(dataUpdatedListener != null) { dataUpdatedListener?.onDataUpdated()
                                        } else {
                                            TingToast(context!!, "State Changed. Please, Try Again", TingToastType.DEFAULT).showToast(
                                                Toast.LENGTH_LONG)
                                            dialog?.dismiss()
                                        }
                                    }
                                } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(
                                    Toast.LENGTH_LONG) }
                            } else { TingToast(context!!, result, TingToastType.ERROR).showToast(
                                Toast.LENGTH_LONG) }
                        }
                    }
                }
                override fun onCancel() {confirmDialog.dismiss() }
            })
            confirmDialog.show(fragmentManager!!, confirmDialog.tag)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog?.window!!.setLayout(width, height)
        }
    }

    public fun setDataUpdatedListener(listener: DataUpdatedListener) {
        dataUpdatedListener = listener
    }
}