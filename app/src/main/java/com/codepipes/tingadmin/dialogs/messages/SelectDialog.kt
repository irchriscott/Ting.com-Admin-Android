package com.codepipes.tingadmin.dialogs.messages

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.others.SelectAdapter
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.utils.Constants
import kotlinx.android.synthetic.main.fragment_select.view.*

class SelectDialog : DialogFragment() {

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var items: List<String>
    private lateinit var selectItemListener: SelectItemListener

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select, null, false)
        val title = arguments?.getString(Constants.CONFIRM_TITLE_KEY)
        if(title != null) { view.dialog_title.text = title }
        view.form_close.setOnClickListener { dialog?.dismiss() }
        view.form_select.layoutManager = LinearLayoutManager(context)
        view.form_select.adapter = SelectAdapter(items, selectItemListener)
        return view
    }

    public fun setItems(data: List<String>, listener: SelectItemListener) {
        items = data
        selectItemListener = listener
    }
}