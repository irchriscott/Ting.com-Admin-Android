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
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_bill_add_extra.view.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddBillExtraDialog : DialogFragment() {

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
        val view = inflater.inflate(R.layout.dialog_bill_add_extra, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()
        val placementId = arguments?.getInt(Constants.PLACEMENT_KEY)

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        view.dialog_button_save.setOnClickListener {

            val name = view.extra_name.text.toString()
            val quantity = view.extra_quantity.text.toString()
            val price = view.extra_single_price.text.toString()

            if(name.isNotBlank() && quantity.isNotEmpty() && price.isNotEmpty()) {

                val data = hashMapOf<String, String>()
                data["price"] = price
                data["quantity"] = quantity
                data["name"] = name

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                TingClient.postRequest(Routes.ordersAddExtra.format(placementId), data, null, session?.token) { _, isSuccess, result ->
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
                        } else { TingToast(context!!, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                    }
                }
            } else { TingToast(context!!,"Fill All Fields", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG) }
        }

        return view
    }

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }
}