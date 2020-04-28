package com.codepipes.tingadmin.fragments.sidebar


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.dialogs.restaurant.UpdateBranchProfileDialog
import com.codepipes.tingadmin.dialogs.restaurant.UpdateCategoriesDialog
import com.codepipes.tingadmin.dialogs.restaurant.UpdateConfigurationsDialog
import com.codepipes.tingadmin.dialogs.restaurant.UpdateRestaurantDialog
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.coursion.freakycoder.mediapicker.galleries.Gallery
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_restaurant.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RestaurantFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    private lateinit var layoutView: View

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_restaurant, container, false).also { layoutView = it }

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        view.restaurant_name.text = "${session.branch.restaurant?.name}, ${session.branch.name}"
        view.restaurant_motto.text = session.branch.restaurant?.motto
        view.restaurant_address.text = session.branch.address
        Picasso.get().load(session.branch.restaurant?.logoURL()).into(view.restaurant_logo)

        view.restaurant_name_else.text = session.branch.restaurant?.name
        view.branch_name.text = session.branch.name
        view.branch_email.text = session.branch.email
        view.branch_phone_number.text = session.branch.phone
        view.restaurant_motto_else.text = session.branch.restaurant?.motto
        view.restaurant_motto_else.textSize = 13.0f
        view.branch_address.text = session.branch.address
        view.restaurant_working_hours.text = "${session.branch.restaurant?.opening} - ${session.branch.restaurant?.closing}"

        view.restaurant_currency.text = session.branch.restaurant?.config?.currency
        view.restaurant_vat.text = "${session.branch.restaurant?.config?.tax} %"
        view.restaurant_late_reservation.text = "${session.branch.restaurant?.config?.cancelLateBooking} minutes"
        view.restaurant_reservation_with_advance.text = if(session.branch.restaurant?.config?.bookWithAdvance!!){ "YES"} else { "NO" }
        view.restaurant_reservation_advance.text = if(session.branch.restaurant?.config?.bookWithAdvance != false){ "${session.branch.restaurant?.config?.currency} ${session.branch.restaurant?.config?.bookingAdvance}"} else { "-" }
        view.restaurant_refund_after_cancelation.text = if(session.branch.restaurant?.config?.bookingCancelationRefund != false) { "YES" } else { "NO" }
        view.restaurant_booking_payment_mode.text = Constants.BOOKING_PAYEMENT_MODE[session.branch.restaurant?.config?.bookingPaymentMode]
        view.restaurant_days_before_booking.text = "${session.branch.restaurant?.config?.daysBeforeReservation} Days"
        view.restaurant_can_take_away.text = if(session.branch.restaurant?.config?.canTakeAway != false) { "YES" } else { "NO" }
        view.restaurant_pay_before.text = if(session.branch.restaurant?.config?.userShouldPayBefore != false) { "YES" } else { "NO" }

        if(!session.permissions.contains("can_update_restaurant")) {
            view.button_edit_restaurant.visibility = View.GONE
            view.button_edit_categories.visibility = View.GONE
            view.button_edit_restaurant.isClickable = false
            view.button_edit_categories.isClickable = false
        }

        if(!session.permissions.contains("can_update_branch")) {
            view.button_edit_branch.visibility = View.GONE
            view.button_edit_branch.isClickable = false
        }

        if(!session.permissions.contains("can_update_configurations")) {
            view.button_edit_config.visibility = View.GONE
            view.button_edit_config.isClickable = false
        }

        view.restaurant_logo.setOnClickListener {
            if(session.permissions.contains("can_update_restaurant")) {
                val confirmDialog = ConfirmDialog()
                val bundle = Bundle()
                bundle.putString(Constants.CONFIRM_TITLE_KEY, "Update Restaurant Logo")
                bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you want to change restaurant logo ?")
                confirmDialog.arguments = bundle
                confirmDialog.onDialogListener(object : ConfirmDialogListener {
                    override fun onAccept() {
                        confirmDialog.dismiss()
                        val intent = Intent(activity, Gallery::class.java)
                        if (ContextCompat.checkSelfPermission(
                                context!!,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            intent.putExtra("title", resources.getString(R.string.edit_user_profile_select_image))
                            intent.putExtra("mode", 2)
                            intent.putExtra("maxSelection", 1)
                            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
                        } else {
                            ActivityCompat.requestPermissions(activity!!,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                REQUEST_CODE_IMAGE_GALLERY
                            )
                        }
                    }
                    override fun onCancel() { confirmDialog.dismiss() }
                })
                confirmDialog.show(fragmentManager!!, confirmDialog.tag)
            }
        }

        view.button_edit_restaurant.setOnClickListener {
            val updateRestaurantDialog = UpdateRestaurantDialog()
            updateRestaurantDialog.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    updateRestaurantDialog.dismiss()
                    userAuthentication.updateSession()
                }
                override fun onCancel() { updateRestaurantDialog.dismiss() }
            })
            updateRestaurantDialog.show(fragmentManager!!, updateRestaurantDialog.tag)
        }

        view.button_edit_branch.setOnClickListener {
            val updateBranchProfileDialog = UpdateBranchProfileDialog()
            updateBranchProfileDialog.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    updateBranchProfileDialog.dismiss()
                    userAuthentication.updateSession()
                }
                override fun onCancel() { updateBranchProfileDialog.dismiss() }
            })
            updateBranchProfileDialog.show(fragmentManager!!, updateBranchProfileDialog.tag)
        }

        view.button_edit_config.setOnClickListener {
            val updateConfigurationsDialog = UpdateConfigurationsDialog()
            updateConfigurationsDialog.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    updateConfigurationsDialog.dismiss()
                    userAuthentication.updateSession()
                }
                override fun onCancel() { updateConfigurationsDialog.dismiss() }
            })
            updateConfigurationsDialog.show(fragmentManager!!, updateConfigurationsDialog.tag)
        }

        view.button_edit_categories.setOnClickListener {
            val updateCategoriesFragment = UpdateCategoriesDialog()
            updateCategoriesFragment.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    updateCategoriesFragment.dismiss()
                    userAuthentication.updateSession()
                }
                override fun onCancel() { updateCategoriesFragment.dismiss() }
            })
            updateCategoriesFragment.show(fragmentManager!!, updateCategoriesFragment.tag)
        }

        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CODE_IMAGE_GALLERY -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(activity, Gallery::class.java)
                    intent.putExtra("title", resources.getString(R.string.edit_user_profile_select_image))
                    intent.putExtra("mode", 2)
                    intent.putExtra("maxSelection", 1)
                    startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectionResult = data.getStringArrayListExtra("result")

                val progressOverlay = ProgressOverlay()

                if(selectionResult.size > 0){
                    try {
                        val image = File(selectionResult[0])
                        val uriFromPath = Uri.fromFile(image)
                        val imageBitmap = BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uriFromPath))

                        layoutView.restaurant_logo.setImageBitmap(imageBitmap)

                        val client = OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build()

                        val mediaTypePng = "image/png".toMediaType()
                        val imageName = "${session.username.toLowerCase()}_${UtilsFunctions.getToken(12).toLowerCase()}.png"

                        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("logo", imageName, RequestBody.create(mediaTypePng, image)).build()

                        val request = Request.Builder()
                            .header("Authorization", session.token)
                            .url(Routes.updateRestaurantLogo)
                            .post(requestBody)
                            .build()

                        progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                        client.newCall(request).enqueue(object : Callback {

                            override fun onFailure(call: Call, e: IOException) {
                                activity?.runOnUiThread {
                                    progressOverlay.dismiss()
                                    Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(layoutView.restaurant_logo)
                                    TingToast(context!!, e.message!!, TingToastType.ERROR).showToast(
                                        Toast.LENGTH_LONG)
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseBody = response.body!!.string()
                                val gson = Gson()
                                try{
                                    val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)
                                    activity?.runOnUiThread {
                                        progressOverlay.dismiss()
                                        if (serverResponse.status == 200){
                                            userAuthentication.updateSession()
                                            TingToast(context!!, serverResponse.message, TingToastType.SUCCESS).showToast(
                                                Toast.LENGTH_LONG)
                                        } else { TingToast(context!!, serverResponse.message, TingToastType.ERROR).showToast(
                                            Toast.LENGTH_LONG) }
                                    }
                                } catch (e: Exception){
                                    activity?.runOnUiThread {
                                        Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(layoutView.restaurant_logo)
                                        progressOverlay.dismiss()
                                        TingToast(context!!, "An Error Has Occurred", TingToastType.ERROR).showToast(
                                            Toast.LENGTH_LONG)
                                    }
                                }
                            }
                        })

                    } catch (e: FileNotFoundException) { TingToast(context!!, e.message!!, TingToastType.ERROR).showToast(
                        Toast.LENGTH_LONG) }

                } else { TingToast(context!!, "No Image Selected", TingToastType.DEFAULT).showToast(
                    Toast.LENGTH_LONG) }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_IMAGE_GALLERY = 6
        private const val REQUEST_CODE_IMAGE_PICKER = 7
    }
}
