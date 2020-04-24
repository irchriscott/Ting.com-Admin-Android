package com.codepipes.tingadmin.adapters.tableview.holders

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractViewHolder

class ActionsCellViewHolder (private val view: View) : AbstractViewHolder(view) {

    private var loadDataButton: Button = view.findViewById(R.id.show_actions)
    private var cellContainer: LinearLayout = view.findViewById(R.id.cell_container)

    public fun setCellModel(cellModel: CellModel, columnPosition: Int) {
        cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        loadDataButton.requestLayout()
    }

    override fun setSelected(selectionState: SelectionState) {
        super.setSelected(selectionState)
    }
}