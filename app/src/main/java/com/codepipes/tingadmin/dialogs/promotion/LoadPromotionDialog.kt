package com.codepipes.tingadmin.dialogs.promotion

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.models.MenuPromotion
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_promotion_load.view.*

class LoadPromotionDialog : DialogFragment() {

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
        val view = inflater.inflate(R.layout.dialog_promotion_load, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val promotion = Gson().fromJson(arguments?.getString(Constants.PROMOTION_KEY), MenuPromotion::class.java)
        Picasso.get().load("${Routes.HOST_END_POINT}${promotion.posterImage}").into(view.promotion_poster)

        view.dialog_button_close.setOnClickListener { dialog?.dismiss() }

        return view
    }

}