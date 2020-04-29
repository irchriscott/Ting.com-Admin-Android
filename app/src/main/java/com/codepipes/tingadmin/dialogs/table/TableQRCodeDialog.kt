package com.codepipes.tingadmin.dialogs.table

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.models.RestaurantTable
import com.codepipes.tingadmin.utils.Constants
import com.google.gson.Gson
import com.google.zxing.EncodeHintType
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_table_qr_code.view.*
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.image.ImageType
import java.io.File
import java.io.FileOutputStream


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TableQRCodeDialog : DialogFragment() {

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
        val view = inflater.inflate(R.layout.dialog_table_qr_code, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val table = Gson().fromJson(arguments?.getString(Constants.TABLE_KEY), RestaurantTable::class.java)
        val tableQRCode = QRCode.from(table.uuid).to(ImageType.JPG).withSize(1000, 1000)
        val croppedBitmap: Bitmap = Bitmap.createBitmap(tableQRCode.bitmap(), 20, 20, tableQRCode.bitmap().width - 40, tableQRCode.bitmap().height - 40)
        Picasso.get().load(tableQRCode.withHint(EncodeHintType.MARGIN, 0).file()).into(view.table_qr_code)

        view.dialog_button_cancel.setOnClickListener { dialog?.dismiss() }
        view.dialog_button_download.setOnClickListener {
            try {
                val filename = "qr_code_table_no_${table.number.toLowerCase()}.jpg"
                val path = File(Environment.getExternalStorageDirectory(), "Ting.com" + File.separator + "Tables")
                if (!path.exists()) { path.mkdirs() }
                val qrCodePicture = File(path, filename)

                val outputStream = FileOutputStream(qrCodePicture)
                tableQRCode.writeTo(outputStream)

                TingToast(context!!, "QR Code Saved !!!", TingToastType.SUCCESS).showToast(Toast.LENGTH_LONG)
                dialog?.dismiss()

            } catch (e: Exception) {  TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
        }

        return view
    }
}