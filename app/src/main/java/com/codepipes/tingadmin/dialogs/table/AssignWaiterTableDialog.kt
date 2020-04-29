package com.codepipes.tingadmin.dialogs.table

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.table.SelectWaiterTableAdapter
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.Waiter
import com.codepipes.tingadmin.utils.Constants
import kotlinx.android.synthetic.main.dialog_table_assign_waiter.view.*

class AssignWaiterTableDialog : DialogFragment() {

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var waiters: List<Waiter>
    private lateinit var selectItemListener: SelectItemListener

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_table_assign_waiter, null, false)

        view.form_close.setOnClickListener { dialog?.dismiss() }
        view.form_select.layoutManager = LinearLayoutManager(context)
        view.form_select.adapter = SelectWaiterTableAdapter(waiters, selectItemListener)

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

    public fun setWaiters(data: List<Waiter>, listener: SelectItemListener) {
        waiters = data
        selectItemListener = listener
    }
}