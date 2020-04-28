package com.codepipes.tingadmin.dialogs.restaurant

import android.annotation.SuppressLint
import android.app.TimePickerDialog
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
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_resto_update_restaurant.view.*
import java.lang.Exception
import java.util.*

class UpdateRestaurantDialog : DialogFragment() {

    private var formDialogListener: FormDialogListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_resto_update_restaurant, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()!!
        view.restaurant_name.setText(session.branch.restaurant?.name)
        view.restaurant_opening.setText(session.branch.restaurant?.opening)
        view.restaurant_closing.setText(session.branch.restaurant?.closing)
        view.restaurant_motto.setText(session.branch.restaurant?.motto)
        view.restaurant_email.setText(session.branch.restaurant?.config?.email)
        view.restaurant_phone.setText(session.branch.restaurant?.config?.phone)

        val calendar = Calendar.getInstance()

        val openingTime = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            view.restaurant_opening.setText("$hourOfDay:$minute")
        }

        view.restaurant_opening.setOnClickListener {
            TimePickerDialog(activity!!,
                R.style.DatePickerAppTheme, openingTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        val closingTime = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            view.restaurant_closing.setText("$hourOfDay:$minute")
        }

        view.restaurant_closing.setOnClickListener {
            TimePickerDialog(activity!!,
                R.style.DatePickerAppTheme, closingTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        view.dialog_button_save.setOnClickListener {

            val data = hashMapOf<String, String>()

            data["name"] = view.restaurant_name.text.toString()
            data["opening"] = view.restaurant_opening.text.toString()
            data["closing"] = view.restaurant_closing.text.toString()
            data["motto"] = view.restaurant_motto.text.toString()
            data["email"] = view.restaurant_email.text.toString()
            data["phone"] = view.restaurant_phone.text.toString()
            data["password"] = view.admin_password.text.toString()

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager!!, progressOverlay.tag)

            TingClient.postRequest(Routes.updateRestaurantProfile, data, null, session.token) { _, isSuccess, result ->
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
                        } catch (e: Exception) { TingToast(context!!, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
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