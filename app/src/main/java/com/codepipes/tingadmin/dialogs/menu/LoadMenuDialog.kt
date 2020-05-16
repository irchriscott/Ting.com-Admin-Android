package com.codepipes.tingadmin.dialogs.menu

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.models.Menu
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_menu_load.view.*

class LoadMenuDialog : DialogFragment() {

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
        val view = inflater.inflate(R.layout.dialog_menu_load, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val menu = Gson().fromJson(arguments?.getString(Constants.MENU_KEY), Menu::class.java)
        view.dialog_title.text = when(menu.type) {
            1 -> "Menu Food"
            2 -> "Menu Drink"
            3 -> "Menu Dish"
            else -> "Menu"
        }

        val index = (0 until menu.images.count - 1).random()
        val image = menu.images.images[index]
        Picasso.get().load("${Routes.HOST_END_POINT}${image.image}").into(view.menu_image)

        view.dialog_button_close.setOnClickListener { dialog?.dismiss() }

        return view
    }
}