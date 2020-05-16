package com.codepipes.tingadmin.dialogs.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.menu.dish.SelectDrinkAdapter
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.Menu
import kotlinx.android.synthetic.main.dialog_table_assign_waiter.view.*

class AddDishDrinkDialog : DialogFragment() {

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var drinks: List<Menu>
    private lateinit var selectItemListener: SelectItemListener

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_table_assign_waiter, null, false)

        view.dialog_title.text = "Add Drink To Dish"
        view.form_close.setOnClickListener { dialog?.dismiss() }
        view.form_select.layoutManager = LinearLayoutManager(context)
        view.form_select.adapter = SelectDrinkAdapter(drinks, selectItemListener)

        return view
    }

    public fun setDrinks(data: List<Menu>, listener: SelectItemListener) {
        drinks = data
        selectItemListener = listener
    }
}