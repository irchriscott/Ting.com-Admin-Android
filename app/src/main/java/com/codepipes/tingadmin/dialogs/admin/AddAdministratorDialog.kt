package com.codepipes.tingadmin.dialogs.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.RestaurantBranch
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_admin_edit.view.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddAdministratorDialog : DialogFragment() {

    private var selectedAdminType: Int? = null
    private var selectedBranchId: Int? = null
    private var formDialogListener: FormDialogListener? = null
    private var branches = mutableListOf<RestaurantBranch>()

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
        val view = inflater.inflate(R.layout.dialog_admin_edit, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()

        view.dialog_title.text = "Add Administrator"
        view.admin_branch.visibility = View.VISIBLE

        view.admin_type_select.setOnClickListener {
            val selectDialog = SelectDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Administrator Type")
            selectDialog.arguments = bundle
            selectDialog.setItems(Constants.ADMIN_TYPE.toSortedMap().map { it.value }, object :
                SelectItemListener {
                override fun onSelectItem(position: Int) {
                    selectedAdminType = position + 1
                    view.selected_admin_type.text = Constants.ADMIN_TYPE[selectedAdminType?:0]
                    view.selected_admin_type.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)
                    selectDialog.dismiss()
                }
            })
            selectDialog.show(fragmentManager!!, selectDialog.tag)
        }

        TingClient.getRequest(Routes.branchesAll, null, session?.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                if(isSuccess) {
                    try { branches.addAll(Gson().fromJson<MutableList<RestaurantBranch>>(result, object : TypeToken<MutableList<RestaurantBranch>>(){}.type))
                    } catch (e: Exception) { }
                }
            }
        }

        view.admin_branch_select.setOnClickListener {
            TingClient.getRequest(Routes.branchesAll, null, session?.token) { _, isSuccess, result ->
                activity?.runOnUiThread {
                    if(isSuccess) {
                        try {
                            val branches = Gson().fromJson<MutableList<RestaurantBranch>>(result, object : TypeToken<MutableList<RestaurantBranch>>(){}.type)
                            val selectDialog = SelectDialog()
                            val bundle = Bundle()
                            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Administrator Branch")
                            selectDialog.arguments = bundle
                            selectDialog.setItems(branches.sortedBy { it.id }.map { it.name }, object :
                                SelectItemListener {
                                override fun onSelectItem(position: Int) {
                                    selectedBranchId = branches.sortedBy { it.id }[position].id
                                    view.selected_admin_branch.text = branches.sortedBy { it.id }[position].name
                                    view.selected_admin_branch.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)
                                    selectDialog.dismiss()
                                }
                            })
                            selectDialog.show(fragmentManager!!, selectDialog.tag)
                        } catch (e: Exception) { }
                    }
                }
            }
        }

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        view.dialog_button_save.setOnClickListener {

            if(selectedAdminType != null && selectedBranchId != null) {

                val data = HashMap<String, String>()

                data["name"] = view.admin_name.text.toString()
                data["username"] = view.admin_username.text.toString()
                data["email"] = view.admin_email.text.toString()
                data["admin_type"] =  selectedAdminType.toString()
                data["badge_number"] = view.admin_badge_number.text.toString().toUpperCase()
                data["phone"] = view.admin_phone_number.text.toString()
                data["password"] = view.admin_password.text.toString()
                data["branch"] = selectedBranchId.toString()

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                TingClient.postRequest(Routes.addNewAdmin, data, null, session?.token) { _, isSuccess, result ->
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
            } else { TingToast(context!!, "Fill All The Form, Please", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
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