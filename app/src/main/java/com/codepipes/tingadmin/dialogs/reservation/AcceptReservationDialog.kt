package com.codepipes.tingadmin.dialogs.reservation

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.reservation.SelectBookingTableAdapter
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.Booking
import com.codepipes.tingadmin.models.RestaurantTable
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_booking_accept.view.*
import java.lang.Exception

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AcceptReservationDialog : DialogFragment() {

    private var formDialogListener: FormDialogListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    private var selectedTable: Int? = null

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
        val view = inflater.inflate(R.layout.dialog_booking_accept, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val gson = Gson()

        val session = UserAuthentication(context!!).get()
        val booking = gson.fromJson(arguments?.getString(Constants.BOOKING_KEY), Booking::class.java)

        TingClient.getRequest(Routes.tablesAll, null, session?.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                if (isSuccess) {
                    try {
                        val tables =
                            gson.fromJson<List<RestaurantTable>>(result, object : TypeToken<List<RestaurantTable>>() {}.type)
                        val tableAdapter = SelectBookingTableAdapter(tables.filter { it.location == booking.location }, object : SelectItemListener{
                            override fun onSelectItem(position: Int) { selectedTable = position }
                        })
                        view.tables_recycle_view.layoutManager = LinearLayoutManager(context)
                        view.tables_recycle_view.adapter = tableAdapter
                    } catch (e: Exception) { }
                }
            }
        }

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        view.dialog_button_save.setOnClickListener {
            if(selectedTable != null) {
                val data = hashMapOf<String, String>()
                data["table"] = selectedTable.toString()

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                TingClient.postRequest("${Routes.reservationAccept}${booking.id}/", data, null, session?.token) { _, isSuccess, result ->
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
                            } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(
                                Toast.LENGTH_LONG) }
                        } else { TingToast(context!!, result, TingToastType.ERROR).showToast(
                                Toast.LENGTH_LONG) }
                    }
                }
            } else { TingToast(context!!, "Please, Select Table", TingToastType.ERROR).showToast(
                Toast.LENGTH_LONG) }
        }

        return view
    }

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }
}