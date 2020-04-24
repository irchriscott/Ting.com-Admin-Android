package com.codepipes.tingadmin.adapters.tableview.holders

import androidx.core.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractViewHolder


class RowHeaderViewHolder(private val view: View) : AbstractViewHolder(view) {

    val rowHeaderTextView: TextView = view.findViewById(R.id.row_header_textview)

    override fun setSelected(selectionState: SelectionState) {
        super.setSelected(selectionState)
    }
}