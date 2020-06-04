package com.codepipes.tingadmin.adapters.tableview.holders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractViewHolder
import java.lang.Exception

class BillOrderActionCellViewHolder (private val view: View) : AbstractViewHolder(view) {

    private var cellContainer: LinearLayout = view.findViewById(R.id.cell_container)
    private var actionAcceptDecline: LinearLayout = view.findViewById(R.id.action_accept_decline)
    private var actionAccepted: ImageView = view.findViewById(R.id.action_accepted)

    public fun setCellModel(cellModel: CellModel, columnPosition: Int) {
        cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        if(!cellModel.data.toBoolean()) {
            actionAcceptDecline.visibility = View.GONE
            actionAccepted.visibility = View.VISIBLE
            actionAccepted.requestLayout()
        }  else {
            actionAcceptDecline.visibility = View.VISIBLE
            actionAccepted.visibility = View.GONE
            actionAcceptDecline.requestLayout()
        }
    }

    override fun setSelected(selectionState: SelectionState) {
        super.setSelected(selectionState)
    }
}