package com.codepipes.tingadmin.dialogs.restaurant

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
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.SelectDialog
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_resto_update_configurations.view.*
import java.lang.Exception


class UpdateConfigurationsDialog : DialogFragment() {

    private var formDialogListener: FormDialogListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    private var selectedCurrency: String = ""
    private var selectedPaymentMode: Int = 3

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_resto_update_configurations, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()!!

        selectedCurrency = session.branch.restaurant?.config!!.currency.toUpperCase()
        selectedPaymentMode = session.branch.restaurant.config.bookingPaymentMode

        view.restaurant_email.setText(session.branch.restaurant.config.email)
        view.selected_currency.text = Constants.CURRENCIES[session.branch.restaurant.config.currency.toUpperCase()]
        view.selected_currency.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)
        view.restaurant_tax_rate.setText(session.branch.restaurant.config.tax.toString())
        view.restaurant_can_take_away.isChecked = !session.branch.restaurant.config.canTakeAway
        view.restaurant_pay_before.isChecked = !session.branch.restaurant.config.userShouldPayBefore
        view.restaurant_book_with_advance.isChecked = !session.branch.restaurant.config.bookWithAdvance
        view.restaurant_reservation_advance.setText(session.branch.restaurant.config.bookingAdvance.toString())
        view.restaurant_refund_after_cancelation.isChecked = !session.branch.restaurant.config.bookingCancelationRefund
        view.restaurant_cancelation_refund.setText(session.branch.restaurant.config.bookingCancelationRefundPercent.toString())
        view.selected_restaurant_booking_payment_mode.text = Constants.BOOKING_PAYEMENT_MODE[session.branch.restaurant.config.bookingPaymentMode]
        view.selected_restaurant_booking_payment_mode.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)
        view.restaurant_days_before_booking.setText(session.branch.restaurant.config.daysBeforeReservation.toString())

        view.restaurant_currency_select.setOnClickListener {
            val currencies = Constants.CURRENCIES
            val currenciesName = currencies.values.toMutableList()

            val selectDialog = SelectDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Currency")
            selectDialog.arguments = bundle
            selectDialog.setItems(currenciesName, object :
                SelectItemListener {
                override fun onSelectItem(position: Int) {
                    view.selected_currency.text = currenciesName[position]
                    for((k, v) in currencies) { if(v == currenciesName[position]) { selectedCurrency = k } }
                    view.selected_currency.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)
                    selectDialog.dismiss()
                }
            })
            selectDialog.show(fragmentManager!!, selectDialog.tag)
        }

        view.restaurant_booking_payment_mode_select.setOnClickListener {
            val modes = Constants.BOOKING_PAYEMENT_MODE
            val selectDialog = SelectDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Payment Mode")
            selectDialog.arguments = bundle
            selectDialog.setItems(modes.toSortedMap().map { it.value }, object :
                SelectItemListener {
                override fun onSelectItem(position: Int) {
                    selectedPaymentMode = position + 1
                    view.selected_restaurant_booking_payment_mode.text = modes[position + 1]
                    view.selected_restaurant_booking_payment_mode.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)
                    selectDialog.dismiss()
                }
            })
            selectDialog.show(fragmentManager!!, selectDialog.tag)
        }

        view.dialog_button_save.setOnClickListener {

            val data = hashMapOf<String, String>()

            data["currency"] = selectedCurrency
            data["use_default_currency"] = "True"
            data["tax"] = view.restaurant_tax_rate.text.toString()
            data["cancel_late_booking"] = view.restaurant_late_reservation.text.toString()
            data["waiter_see_all_orders"] = "False"
            data["book_with_advance"] = view.restaurant_book_with_advance.text.toString()
            data["booking_advance"] = view.restaurant_reservation_advance.text.toString()
            data["booking_cancelation_refund"] = view.restaurant_refund_after_cancelation.text.toString()
            data["booking_cancelation_refund_percent"] = view.restaurant_cancelation_refund.text.toString()
            data["booking_payement_mode"] = selectedPaymentMode.toString()
            data["days_before_reservation"] = view.restaurant_days_before_booking.text.toString()
            data["can_take_away"] = view.restaurant_can_take_away.text.toString()
            data["user_should_pay_before"] = view.restaurant_pay_before.text.toString()
            data["password"] = view.admin_password.text.toString()

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager!!, progressOverlay.tag)

            TingClient.postRequest(Routes.updateRestaurantConfig, data, null, session.token) { _, isSuccess, result ->
                activity?.runOnUiThread {
                    progressOverlay.dismiss()
                    if(isSuccess) {
                        try {
                            val serverResponse = Gson().fromJson(result, ServerResponse::class.java)
                            TingToast(context!!, serverResponse.message, if(serverResponse.type == "success") { TingToastType.SUCCESS } else { TingToastType.ERROR }).showToast(
                                Toast.LENGTH_LONG)
                            if(serverResponse.type == "success") {
                                if(formDialogListener != null) { formDialogListener?.onSave()
                                } else {
                                    TingToast(context!!, "State Changed. Please, Try Again", TingToastType.DEFAULT).showToast(
                                        Toast.LENGTH_LONG)
                                    dialog?.dismiss()
                                }
                            }
                        } catch (e: Exception) { TingToast(context!!, result, TingToastType.ERROR).showToast(
                            Toast.LENGTH_LONG) }
                    } else { TingToast(context!!, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                }
            }
        }

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
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

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }
}
