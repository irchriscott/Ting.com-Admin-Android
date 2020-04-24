package com.codepipes.tingadmin.dialogs.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_admin_load.view.*

class LoadAdministratorDialog : DialogFragment() {

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_admin_load, container, false)

        val administrator = Gson().fromJson(arguments?.getString(Constants.ADMIN_KEY), Administrator::class.java)
        view.admin_name.text = administrator.name
        view.admin_username.text = administrator.username.toLowerCase()
        view.admin_branch.text = administrator.branch.name
        view.admin_email.text = administrator.email
        view.admin_phone_number.text = administrator.phone
        view.admin_badge_number.text = administrator.badgeNumber.toUpperCase()
        view.admin_type.text = Constants.ADMIN_TYPE[administrator.type.toInt()]
        view.admin_created_date.text = UtilsFunctions.formatDate(administrator.createdAt)
        Picasso.get().load("${Routes.HOST_END_POINT}${administrator.image}").into(view.admin_image)

        view.dialog_button_close.setOnClickListener { dialog?.dismiss() }

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
}