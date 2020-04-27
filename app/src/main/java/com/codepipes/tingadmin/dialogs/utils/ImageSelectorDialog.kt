package com.codepipes.tingadmin.dialogs.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.interfaces.ImageSelectorListener
import com.coursion.freakycoder.mediapicker.galleries.Gallery
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_image_selector.view.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ImageSelectorDialog : DialogFragment() {

    private var maxImages: Int = 1
    private var selectorType: Int = 0
    private var imageSelectorListener: ImageSelectorListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    private var filePath: String = ""

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_image_selector, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        view.open_camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_CAMERA
                )
            }
        }

        view.camera_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_CAMERA
                )
            }
        }

        view.gallery_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_IMAGE_GALLERY
                )
            }
        }

        view.open_gallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_IMAGE_GALLERY
                )
            }
        }

        view.dialog_cancel.setOnClickListener { imageSelectorListener?.onCancel() }
        view.dialog_confirm.setOnClickListener {
            if(selectorType == 0) { imageSelectorListener?.onSingleImageSelected(filePath) }
            else { imageSelectorListener?.onCancel() }
        }

        return view
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = dialog?.context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply { filePath = absolutePath }
    }

    public fun setOnImageSelectorListener(listener: ImageSelectorListener) {
        imageSelectorListener = listener
    }

    public fun setSelectorType(type: Int) {
        selectorType = type
    }

    public fun setMaxImages(max: Int) {
        maxImages = max
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(context!!.packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) { null }

                photoFile?.also { file ->
                    val photoURI: Uri = FileProvider.getUriForFile(context!!, "com.codepipes.tingadmin.fileprovider", file)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, PICK_GALLERY_RESULT_CODE)
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(activity, Gallery::class.java)
        intent.putExtra("title", if(selectorType == 0) { resources.getString(R.string.edit_user_profile_select_image) } else { "Select Images" })
        intent.putExtra("mode", 2)
        intent.putExtra("maxSelection", maxImages)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CODE_IMAGE_GALLERY -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){ openGallery() }
            }
            REQUEST_CODE_CAMERA -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){ openCamera() }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                PICK_GALLERY_RESULT_CODE -> {
                    if (resultCode == RESULT_OK && data != null) {
                        imageSelectorListener?.onSingleImageSelected(filePath)
                    }
                }
                REQUEST_CODE_IMAGE_PICKER -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val selectionResult = data.getStringArrayListExtra("result")
                        if (selectionResult.size > 0) {
                            if(selectorType == 0) {
                                val image = File(selectionResult[0])
                                imageSelectorListener?.onSingleImageSelected(image.absolutePath)
                            } else {
                                val images = mutableListOf<String>()
                                selectionResult.forEach {
                                    try { images.add(File(it).absolutePath)
                                    } catch (e: Exception) {}
                                }
                                imageSelectorListener?.onMultipleImagesSelected(images)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val PICK_GALLERY_RESULT_CODE = 2
        private const val REQUEST_CODE_CAMERA = 5
        private const val REQUEST_CODE_IMAGE_PICKER = 216
        private const val REQUEST_CODE_IMAGE_GALLERY = 982
    }
}