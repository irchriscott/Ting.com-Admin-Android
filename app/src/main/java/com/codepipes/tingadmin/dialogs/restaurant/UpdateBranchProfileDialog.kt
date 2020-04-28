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
import com.codepipes.tingadmin.models.BranchSpecial
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_resto_update_branch.view.*
import kotlinx.android.synthetic.main.row_checkbox.view.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class UpdateBranchProfileDialog : DialogFragment() {

    private val specials = mutableListOf<Int>()
    private val services = mutableListOf<Int>()
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
        val view = inflater.inflate(R.layout.dialog_resto_update_branch, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()!!

        view.branch_name.setText(session.branch.name)
        view.branch_email.setText(session.branch.email)
        view.branch_phone_number.setText(session.branch.phone)
        services.addAll(session.branch.services.map { it.id })
        specials.addAll(session.branch.specials.map { it.id })

        Constants.RESTAURANT_SPECIALS.forEach { view.branch_specials.addView(specialServiceView(it, 0)) }
        Constants.RESTAURANT_SERVICES.forEach { view.branch_services.addView(specialServiceView(it, 1)) }

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

            specials.forEach { formBuilder.addFormDataPart("specials", it.toString()) }
            services.forEach { formBuilder.addFormDataPart("services", it.toString()) }

            formBuilder.addFormDataPart("name", view.branch_name.text.toString())
            formBuilder.addFormDataPart("email", view.branch_email.text.toString())
            formBuilder.addFormDataPart("phone", view.branch_phone_number.text.toString())
            formBuilder.addFormDataPart("region", session.branch.region)
            formBuilder.addFormDataPart("road", session.branch.road)
            formBuilder.addFormDataPart("password", view.admin_password.text.toString())

            val form = formBuilder.build()
            val requestBuilder = Request.Builder().url(Routes.updateBranchProfile).post(form)
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

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog?.window!!.setLayout(width, height)
        }
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun specialServiceView(special: BranchSpecial, type: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.row_checkbox, null, false)
        view.filter_checkbox.isChecked = when(type) {
            0 -> specials.contains(special.id)
            1 -> services.contains(special.id)
            else -> false
        }
        view.filter_name.text = special.name
        view.filter_checkbox.isClickable = true
        view.filter_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                when(type) {
                    0 -> specials.add(special.id)
                    1 -> services.add(special.id)
                }
            }
            else {
                when(type) {
                    0 -> specials.remove(special.id)
                    1 -> services.remove(special.id)
                }
            }
        }
        return view
    }

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }
}
