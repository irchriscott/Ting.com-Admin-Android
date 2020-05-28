package com.codepipes.tingadmin.dialogs.messages

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.others.MultipleSelectAdapter
import com.codepipes.tingadmin.interfaces.MultipleSelectItemsListener
import com.codepipes.tingadmin.utils.Constants
import kotlinx.android.synthetic.main.fragment_multiple_select.view.*

class MultipleSelectDialog : DialogFragment() {

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var items: List<String> = listOf()
    private var selectItemListener: MultipleSelectItemsListener? = null
    private var defaultValues: List<Int>? = null

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_multiple_select, null, false)
        val title = arguments?.getString(Constants.CONFIRM_TITLE_KEY)
        if(title != null) { view.dialog_title.text = title }

        val multipleSelectAdapter = MultipleSelectAdapter(items, defaultValues)

        view.form_close.setOnClickListener { dialog?.dismiss() }
        view.form_select.layoutManager = LinearLayoutManager(context)
        view.form_select.adapter = multipleSelectAdapter
        view.form_submit.setOnClickListener { selectItemListener?.onSelectItems(multipleSelectAdapter.getSelectedItems()) }

        return view
    }

    public fun setItems(data: List<String>, default: List<Int>? = null, listener: MultipleSelectItemsListener) {
        items = data
        selectItemListener = listener
        defaultValues = default
    }
}