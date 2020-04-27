package com.codepipes.tingadmin.dialogs.category

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
import com.codepipes.tingadmin.dialogs.utils.ImageSelectorDialog
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.interfaces.ImageSelectorListener
import com.codepipes.tingadmin.models.FoodCategory
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_category_edit.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddCategoryDialog : DialogFragment() {

    private var formDialogListener: FormDialogListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    private var categoryImagePath: String = ""

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_category_edit, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()
        view.dialog_title.text = "Add Category"

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        view.select_category_image.setOnClickListener {
            val imageSelectDialog = ImageSelectorDialog()
            imageSelectDialog.setMaxImages(1)
            imageSelectDialog.setSelectorType(0)
            imageSelectDialog.setOnImageSelectorListener(object : ImageSelectorListener {
                override fun onMultipleImagesSelected(images: List<String>) {}
                override fun onSingleImageSelected(image: String) {
                    if(image != "") {
                        try {
                            categoryImagePath = image
                            val categoryImage = File(image)
                            view.category_image.visibility = View.VISIBLE
                            Picasso.get().load(categoryImage).into(view.category_image)
                            imageSelectDialog.dismiss()
                        } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.DEFAULT).showToast(
                            Toast.LENGTH_LONG) }
                    } else { TingToast(context!!, "Image Cannot Be Null", TingToastType.DEFAULT).showToast(
                        Toast.LENGTH_LONG) }
                }
                override fun onCancel() { imageSelectDialog.dismiss() }
            })
            imageSelectDialog.show(fragmentManager!!, imageSelectDialog.tag)
        }

        view.dialog_button_save.setOnClickListener {

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager!!, progressOverlay.tag)

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("name", view.category_name.text.toString())
                .addFormDataPart("description", view.category_description.text.toString())

            if(categoryImagePath != "") {
                try {
                    val image = UtilsFunctions.compressFile(File(categoryImagePath))
                    val mediaTypePng = "image/png".toMediaType()
                    requestBodyBuilder.addFormDataPart("image", image?.name, RequestBody.create(mediaTypePng, image!!))
                } catch (e: Exception) {}
            }

            val requestBody = requestBodyBuilder.build()

            val request = Request.Builder()
                .header("Authorization", session!!.token)
                .url(Routes.addNewCategory)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        TingToast(context!!, e.message!!, TingToastType.ERROR).showToast(Toast.LENGTH_LONG)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body!!.string()
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        try {
                            val serverResponse = Gson().fromJson(responseBody, ServerResponse::class.java)
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

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }

}