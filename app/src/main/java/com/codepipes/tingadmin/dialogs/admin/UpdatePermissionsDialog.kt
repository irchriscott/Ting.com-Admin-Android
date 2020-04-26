package com.codepipes.tingadmin.dialogs.admin

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
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
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Permission
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.LocalData
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_admin_permissions.view.*
import kotlinx.android.synthetic.main.row_checkbox.view.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class UpdatePermissionsDialog : DialogFragment() {

    private lateinit var layoutView: View

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    private lateinit var localData: LocalData
    private lateinit var administrator: Administrator

    private var handler: Handler? = null
    private val adminPermissions = mutableListOf<String>()

    private var formDialogListener: FormDialogListener? = null

    private val runnable = Runnable {
        val permissions = localData.getPermissions()
        if(permissions.isNotEmpty()) { setupPermissions(permissions, layoutView) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        layoutView = inflater.inflate(R.layout.dialog_admin_permissions, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        localData = LocalData(context!!)
        administrator = Gson().fromJson(arguments?.getString(Constants.ADMIN_KEY), Administrator::class.java)
        adminPermissions.addAll(administrator.permissions)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        handler = Handler()
        handler?.postDelayed(runnable, 2000)

        val permissions = localData.getPermissions()

        TingClient.getRequest(Routes.permissionsAll, null, null) { _, isSuccess, result ->
            if(isSuccess) {
                activity?.runOnUiThread {
                    val perms = Gson().fromJson<MutableList<Permission>>(result, object : TypeToken<MutableList<Permission>>(){}.type)
                    localData.savePermissions(Gson().toJson(perms))
                    if(permissions.isEmpty()) { handler?.postDelayed(runnable, 2000) }
                }
            }
        }

        layoutView.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }
        layoutView.dialog_button_save.setOnClickListener {

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager!!, progressOverlay.tag)

            val url = "${Routes.updateAdminPermissions}${administrator.token}/"

            val clientBuilder = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .callTimeout(60 * 5, TimeUnit.SECONDS)

            val client = clientBuilder.build()

            val formBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            adminPermissions.forEach { formBuilder.addFormDataPart("permission[]", it) }
            formBuilder.addFormDataPart("password", layoutView.admin_password.text.toString())

            val form = formBuilder.build()
            val requestBuilder = Request.Builder().url(url).post(form)
            requestBuilder.header("Authorization", session.token)
            val request = requestBuilder.build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val dataString = response.body!!.string()
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        try {
                            val serverResponse = Gson().fromJson(dataString, ServerResponse::class.java)
                            TingToast(context!!, serverResponse.message, if(serverResponse.type == "success") { TingToastType.SUCCESS } else { TingToastType.ERROR }).showToast(Toast.LENGTH_LONG)
                            if(serverResponse.type == "success") {
                                if(formDialogListener != null) { formDialogListener?.onSave()
                                } else {
                                    TingToast(context!!, "State Changed. Please, Try Again", TingToastType.DEFAULT).showToast(
                                        Toast.LENGTH_LONG)
                                    dialog?.dismiss()
                                }
                            }
                        } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                    }
                }
            })
        }

        return layoutView
    }

    private fun setupPermissions(permissions: MutableList<Permission>, view: View) {

        view.perms_restaurants.removeAllViews()
        view.perms_administrators.removeAllViews()
        view.perms_tables.removeAllViews()
        view.perms_promotions.removeAllViews()
        view.perms_bills.removeAllViews()
        view.perms_booking.removeAllViews()
        view.perms_categories.removeAllViews()
        view.perms_branches.removeAllViews()
        view.perms_orders.removeAllViews()
        view.perms_menus.removeAllViews()
        view.perms_management.removeAllViews()
        view.perms_placement.removeAllViews()

        val restaurantPerms = permissions.filter { it.category == "restaurant" }
        val administratorPerms = permissions.filter { it.category == "admin" }
        val tablePerms = permissions.filter { it.category == "table" }
        val promotionPerms = permissions.filter { it.category == "promotion" }
        val billPerms = permissions.filter { it.category == "bill" }
        val bookingPerms = permissions.filter { it.category == "booking" }

        val categoryPerms = permissions.filter { it.category == "category" }
        val branchPerms = permissions.filter { it.category == "branch" }
        val orderPerms = permissions.filter { it.category == "orders" }
        val menusPerms = permissions.filter { it.category == "menu" }
        val managementPerms = permissions.filter { it.category == "management" }
        val placementPerms = permissions.filter { it.category == "placement" }

        if(administrator.branch.type != 1) {
            view.perms_tables_view.visibility = View.GONE
            view.perms_bills_view.visibility = View.GONE
            view.perms_booking_view.visibility = View.GONE
            view.perms_orders_view.visibility = View.GONE
            view.perms_management_view.visibility = View.GONE
            view.perms_placement_view.visibility = View.GONE
        }

        restaurantPerms.forEach { view.perms_restaurants.addView(permissionView(it)) }
        administratorPerms.forEach { view.perms_administrators.addView(permissionView(it)) }
        tablePerms.forEach { view.perms_tables.addView(permissionView(it)) }
        promotionPerms.forEach { view.perms_promotions.addView(permissionView(it)) }
        billPerms.forEach { view.perms_bills.addView(permissionView(it)) }
        bookingPerms.forEach { view.perms_booking.addView(permissionView(it)) }

        categoryPerms.forEach { view.perms_categories.addView(permissionView(it)) }
        branchPerms.forEach { view.perms_branches.addView(permissionView(it)) }
        orderPerms.forEach { view.perms_orders.addView(permissionView(it)) }
        menusPerms.forEach { view.perms_menus.addView(permissionView(it)) }
        managementPerms.forEach { view.perms_management.addView(permissionView(it)) }
        placementPerms.forEach { view.perms_placement.addView(permissionView(it)) }
    }

    @SuppressLint("InflateParams")
    private fun permissionView(permission: Permission): View {
        val view = LayoutInflater.from(context).inflate(R.layout.row_checkbox, null, false)
        view.filter_checkbox.isChecked = adminPermissions.contains(permission.permission)
        view.filter_name.text = permission.title
        view.filter_checkbox.isClickable = true
        view.filter_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { adminPermissions.add(permission.permission) }
            else { adminPermissions.remove(permission.permission) }
        }
        return view
    }

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog?.window!!.setLayout(width, height)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        handler?.removeCallbacks(runnable)
    }
}