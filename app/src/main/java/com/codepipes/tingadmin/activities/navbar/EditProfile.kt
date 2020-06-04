package com.codepipes.tingadmin.activities.navbar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.admin.EditAdministratorDialog
import com.codepipes.tingadmin.dialogs.messages.ConfirmDialog
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.PubnubNotification
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.coursion.freakycoder.mediapicker.galleries.Gallery
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)
class EditProfile : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    @SuppressLint("PrivateResource", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        setSupportActionBar(toolbar)

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0F
        supportActionBar?.title = "Edit Profile".toUpperCase()

        try {
            val upArrow = ContextCompat.getDrawable(this@EditProfile,
                R.drawable.abc_ic_ab_back_material
            )
            upArrow!!.setColorFilter(ContextCompat.getColor(this@EditProfile,
                R.color.colorPrimary
            ), PorterDuff.Mode.SRC_ATOP)
            supportActionBar!!.setHomeAsUpIndicator(upArrow)
        } catch (e: java.lang.Exception) {}

        userAuthentication = UserAuthentication(this@EditProfile)
        session = userAuthentication.get()!!

        PubnubNotification.getInstance(this@EditProfile, main_container).initialize()

        admin_name.text = session.name
        admin_username.text = session.username.toLowerCase()
        admin_email.text = session.email
        Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(admin_image)

        admin_image.setOnClickListener {
            val confirmDialog = ConfirmDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Update Profile Image")
            bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you want to change your profile image ?")
            confirmDialog.arguments = bundle
            confirmDialog.onDialogListener(object : ConfirmDialogListener {
                override fun onAccept() {
                    confirmDialog.dismiss()
                    val intent = Intent(this@EditProfile, Gallery::class.java)
                    if (ContextCompat.checkSelfPermission(
                            this@EditProfile,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        intent.putExtra("title", resources.getString(R.string.edit_user_profile_select_image))
                        intent.putExtra("mode", 2)
                        intent.putExtra("maxSelection", 1)
                        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
                    } else {
                        ActivityCompat.requestPermissions(this@EditProfile,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_CODE_IMAGE_GALLERY
                        )
                    }
                }
                override fun onCancel() { confirmDialog.dismiss() }
            })
            confirmDialog.show(supportFragmentManager, confirmDialog.tag)
        }

        admin_name_else.text = session.name
        admin_username_else.text = session.username
        admin_email_else.text = session.email
        admin_phone_number.text = session.phone
        admin_badge_number.text = session.badgeNumber.toUpperCase()
        admin_role.text = Constants.ADMIN_TYPE[session.type.toInt()]
        admin_created_date.text = UtilsFunctions.formatDate(session.createdAt)

        if(!session.permissions.contains("can_update_admin")) {
            button_edit_admin.isEnabled = false
            button_edit_admin.visibility = View.GONE
        }

        button_edit_admin.setOnClickListener {
            val editAdministratorDialog = EditAdministratorDialog()
            val bundle = Bundle()
            bundle.putString(Constants.ADMIN_KEY, Gson().toJson(session))
            editAdministratorDialog.arguments = bundle
            editAdministratorDialog.setFormDialogListener(object : FormDialogListener {
                override fun onCancel() { editAdministratorDialog.dismiss() }
                override fun onSave() {
                    editAdministratorDialog.dismiss()
                    userAuthentication.updateSession()
                }
            })
            editAdministratorDialog.show(supportFragmentManager, editAdministratorDialog.tag)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CODE_IMAGE_GALLERY -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(this@EditProfile, Gallery::class.java)
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
                        val imageBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uriFromPath))

                        admin_image.setImageBitmap(imageBitmap)

                        val client = OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build()

                        val mediaTypePng = "image/png".toMediaType()
                        val imageName = "${session.username.toLowerCase()}_${UtilsFunctions.getToken(12).toLowerCase()}.png"

                        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("image", imageName, RequestBody.create(mediaTypePng, image)).build()

                        val request = Request.Builder()
                            .header("Authorization", session.token)
                            .url(Routes.updateAdminImage)
                            .post(requestBody)
                            .build()

                        progressOverlay.show(supportFragmentManager, progressOverlay.tag)

                        client.newCall(request).enqueue(object : Callback {

                            override fun onFailure(call: Call, e: IOException) {
                                runOnUiThread {
                                    progressOverlay.dismiss()
                                    Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(admin_image)
                                    TingToast(this@EditProfile, e.message!!, TingToastType.ERROR).showToast(Toast.LENGTH_LONG)
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseBody = response.body!!.string()
                                val gson = Gson()
                                try{
                                    val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)
                                    runOnUiThread {
                                        progressOverlay.dismiss()
                                        if (serverResponse.status == 200){
                                            userAuthentication.updateSession()
                                            TingToast(this@EditProfile, serverResponse.message, TingToastType.SUCCESS).showToast(Toast.LENGTH_LONG)
                                        } else { TingToast(this@EditProfile, serverResponse.message, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                                    }
                                } catch (e: Exception){
                                    runOnUiThread {
                                        Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(admin_image)
                                        progressOverlay.dismiss()
                                        TingToast(this@EditProfile, "An Error Has Occurred", TingToastType.ERROR).showToast(Toast.LENGTH_LONG)
                                    }
                                }
                            }
                        })

                    } catch (e: FileNotFoundException) { TingToast(this@EditProfile, e.message!!, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }

                } else { TingToast(this@EditProfile, "No Image Selected", TingToastType.DEFAULT).showToast(Toast.LENGTH_LONG) }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Bridge.saveInstanceState(this, outState)
        outState.clear()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        Bridge.saveInstanceState(this, outState)
        outState.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { outPersistentState?.clear() }
    }

    override fun onDestroy() {
        super.onDestroy()
        Bridge.clear(this)
    }

    companion object {
        private const val REQUEST_CODE_IMAGE_GALLERY = 6
        private const val REQUEST_CODE_IMAGE_PICKER = 7
    }
}
