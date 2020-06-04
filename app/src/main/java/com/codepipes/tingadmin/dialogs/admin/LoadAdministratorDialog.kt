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
import com.codepipes.tingadmin.models.Waiter
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_admin_load.view.*

class LoadAdministratorDialog : DialogFragment() {

    private var type: Int = 0

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_admin_load, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        type =  arguments?.getInt("type", 0)!!
        val session = UserAuthentication(context!!).get()

        if(type == 0) {
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
        } else {
            val waiter = Gson().fromJson(arguments?.getString(Constants.ADMIN_KEY), Waiter::class.java)
            view.dialog_title.text = "Waiter Profile"
            view.admin_name.text = waiter.name
            view.admin_username.text = waiter.username.toLowerCase()
            view.admin_branch.text = session?.branch?.name
            view.admin_email.text = waiter.email
            view.admin_phone_number.text = waiter.phone
            view.admin_badge_number.text = waiter.badgeNumber.toUpperCase()
            view.admin_type.text = waiter.type
            view.admin_created_date.text = UtilsFunctions.formatDate(waiter.createdAt)
            Picasso.get().load("${Routes.HOST_END_POINT}${waiter.image}").into(view.admin_image)
        }

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

    public  fun setType(value: Int) {
        type = value
    }
}