package com.codepipes.tingadmin.adapters.tableview.holders

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.codepipes.tingadmin.utils.Routes
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class TableWaiterCellViewHolder (private val view: View) : AbstractViewHolder(view) {

    private var addWaiterButton: Button = view.findViewById(R.id.add_waiter)
    private var waiterView: LinearLayout = view.findViewById(R.id.waiter_view)
    private var waiterImageView: CircleImageView = view.findViewById(R.id.waiter_image)
    private var waiterNameView: TextView = view.findViewById(R.id.waiter_name)
    private var cellContainer: LinearLayout = view.findViewById(R.id.cell_container)

    @SuppressLint("SetTextI18n")
    public fun setCellModel(cellModel: CellModel, columnPosition: Int) {

        cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT

        if(cellModel.data != "" && cellModel.data != "NULL") {
            val data = cellModel.data.split("::")
            Picasso.get().load("${Routes.HOST_END_POINT}${data[0]}").into(waiterImageView)

            val waiterNames = data[1].split(" ")
            if(waiterNames.size > 1) {
                waiterNameView.text = "${waiterNames[0]} ${waiterNames[1][0].toUpperCase()}."
            } else { waiterNameView.text = data[1] }

            waiterNameView.text = data[1]

            addWaiterButton.visibility = View.GONE
            waiterView.visibility = View.VISIBLE
            waiterView.requestLayout()
            waiterImageView.requestLayout()
            waiterNameView.requestLayout()
        } else {
            addWaiterButton.visibility = View.VISIBLE
            waiterView.visibility = View.GONE
            addWaiterButton.requestLayout()
        }
    }

    override fun setSelected(selectionState: SelectionState) {
        super.setSelected(selectionState)
    }
}