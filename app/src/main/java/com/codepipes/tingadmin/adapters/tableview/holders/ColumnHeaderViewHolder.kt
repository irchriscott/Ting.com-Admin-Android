package com.codepipes.tingadmin.adapters.tableview.holders

import androidx.core.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.tableview.ITableView
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder


class ColumnHeaderViewHolder(private val view: View, val tableView: ITableView) : AbstractSorterViewHolder(view) {

    private var columnHeaderContainer: LinearLayout = view.findViewById(R.id.column_header_container)
    private var columnHeaderTextView: TextView = view.findViewById(R.id.column_header_textView)

    fun setColumnHeaderModel(columnHeaderModel: ColumnHeaderModel, columnPosition: Int) {
        columnHeaderTextView.gravity = Gravity.CENTER_VERTICAL
        columnHeaderTextView.text = columnHeaderModel.data
        columnHeaderContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        columnHeaderTextView.requestLayout()
    }

    override fun setSelected(selectionState: SelectionState) {
        super.setSelected(selectionState)
    }
}