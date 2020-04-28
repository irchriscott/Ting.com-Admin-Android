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
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Permission
import com.codepipes.tingadmin.models.RestaurantCategory
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_resto_update_categories.view.*
import kotlinx.android.synthetic.main.row_checkbox.view.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class UpdateCategoriesDialog : DialogFragment() {

    private var formDialogListener: FormDialogListener? = null
    private val selectedCategories = mutableListOf<Int>()

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
        val view = inflater.inflate(R.layout.dialog_resto_update_categories, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()!!
        selectedCategories.addAll(session.branch.restaurant?.categories?.categories?.map { it.id }!!)

        TingClient.getRequest(Routes.restaurantCategoriesAll, null, null) { _, isSuccess, result ->
            activity?.runOnUiThread {
                if(isSuccess) {
                    try {
                        val categories = Gson().fromJson<List<RestaurantCategory>>(result, object : TypeToken<List<RestaurantCategory>>(){}.type)
                        categories.forEach { view.categories_list_view.addView(categoryView(it)) }
                    } catch (e: Exception) {}
                }
            }
        }

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        view.dialog_button_save.setOnClickListener {

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager!!, progressOverlay.tag)

            val clientBuilder = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .callTimeout(60 * 5, TimeUnit.SECONDS)

            val client = clientBuilder.build()

            val formBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            selectedCategories.forEach { formBuilder.addFormDataPart("categories", it.toString()) }
            formBuilder.addFormDataPart("password", view.admin_password.text.toString())

            val form = formBuilder.build()
            val requestBuilder = Request.Builder().url(Routes.updateRestaurantCategories).post(form)
            requestBuilder.header("Authorization", session.token)

            val request = requestBuilder.build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(
                            Toast.LENGTH_LONG)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val dataString = response.body!!.string()
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        try {
                            val serverResponse = Gson().fromJson(dataString, ServerResponse::class.java)
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
                    }
                }
            })
        }

        return view
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun categoryView(category: RestaurantCategory): View {
        val view = LayoutInflater.from(context).inflate(R.layout.row_checkbox, null, false)
        view.filter_checkbox.isChecked = selectedCategories.contains(category.id)
        view.filter_name.text = "${category.name} (${category.country})"
        view.filter_checkbox.isClickable = true
        view.filter_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { selectedCategories.add(category.id) }
            else { selectedCategories.remove(category.id) }
        }
        return view
    }

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }
}
