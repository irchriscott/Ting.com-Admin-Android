package com.codepipes.tingadmin.dialogs.table

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
import com.codepipes.tingadmin.models.RestaurantTable
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_table_edit.view.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddTableDialog : DialogFragment() {

    private var selectedTableLocation: Int? = null
    private var selectedChairType: Int? = null
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
        val view = inflater.inflate(R.layout.dialog_table_edit, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()!!

        view.table_location_select.setOnClickListener {
            val selectDialog = SelectDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Table Location")
            selectDialog.arguments = bundle
            selectDialog.setItems(Constants.TABLE_LOCATION.toSortedMap().map { it.value }, object :
                SelectItemListener {
                override fun onSelectItem(position: Int) {
                    selectedTableLocation = position + 1
                    view.selected_table_location.text = Constants.TABLE_LOCATION[selectedTableLocation?:1]
                    view.selected_table_location.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)
                    selectDialog.dismiss()
                }
            })
            selectDialog.show(fragmentManager!!, selectDialog.tag)
        }
        view.table_chair_type_select.setOnClickListener {
            val selectDialog = SelectDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Chair Type")
            selectDialog.arguments = bundle
            selectDialog.setItems(Constants.CHAIR_TYPE.toSortedMap().map { it.value }, object :
                SelectItemListener {
                override fun onSelectItem(position: Int) {
                    selectedChairType = position + 1
                    view.selected_table_chair_type.text = Constants.CHAIR_TYPE[selectedChairType?:1]
                    view.selected_table_chair_type.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)
                    selectDialog.dismiss()
                }
            })
            selectDialog.show(fragmentManager!!, selectDialog.tag)
        }

        view.dialog_button_save.setOnClickListener {

            if(selectedChairType != null && selectedTableLocation != null) {

                val data = HashMap<String, String>()

                data["number"] = view.table_number.text.toString().toUpperCase()
                data["max_people"] = view.table_max_people.text.toString()
                data["location"] = selectedTableLocation.toString()
                data["chair_type"] =  selectedChairType.toString()
                data["description"] = view.table_description.text.toString()

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                TingClient.postRequest(Routes.addNewTable, data, null, session.token) { _, isSuccess, result ->
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
            } else { TingToast(context!!, "Select All Required Fields", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
         }

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.cancel() }
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