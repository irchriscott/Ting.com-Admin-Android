package com.codepipes.tingadmin.dialogs.promotion

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.promotion.PromotionMenuAdapter
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.RestaurantMenu
import com.codepipes.tingadmin.utils.Constants
import kotlinx.android.synthetic.main.dialog_table_assign_waiter.view.*

class SelectMenuDialog : DialogFragment() {

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var menus: List<RestaurantMenu>
    private lateinit var selectItemListener: SelectItemListener

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_table_assign_waiter, null, false)
        view.dialog_title.text = arguments?.getString(Constants.CONFIRM_TITLE_KEY)
        view.form_close.setOnClickListener { dialog?.dismiss() }
        view.form_select.layoutManager = LinearLayoutManager(context)
        view.form_select.adapter = PromotionMenuAdapter(menus, selectItemListener)

        return view
    }

    public fun setMenus(data: List<RestaurantMenu>, listener: SelectItemListener) {
        menus = data
        selectItemListener = listener
    }
}