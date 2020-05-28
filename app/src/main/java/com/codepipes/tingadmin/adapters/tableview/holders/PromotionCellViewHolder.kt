package com.codepipes.tingadmin.adapters.tableview.holders

import android.annotation.SuppressLint
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.codepipes.tingadmin.utils.Routes
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PromotionCellViewHolder (private val view: View) : AbstractViewHolder(view) {

    private var promotionTypeText: TextView = view.findViewById(R.id.promotion_type)
    private var menuView: LinearLayout = view.findViewById(R.id.menu_view)
    private var menuImageView: CircleImageView = view.findViewById(R.id.menu_image)
    private var menuNameView: TextView = view.findViewById(R.id.menu_name)
    private var cellContainer: RelativeLayout = view.findViewById(R.id.cell_container)

    @SuppressLint("SetTextI18n")
    public fun setCellModel(cellModel: CellModel, columnPosition: Int) {

        cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT

        if(cellModel.data != "" && cellModel.data != "NULL") {
            if(cellModel.data.contains("::")) {

                val data = cellModel.data.split("::")
                Picasso.get().load("${Routes.HOST_END_POINT}${data[0]}").into(menuImageView)
                menuNameView.text = data[1]

                promotionTypeText.visibility = View.GONE
                menuView.visibility = View.VISIBLE
                menuView.requestLayout()
                menuImageView.requestLayout()
                menuNameView.requestLayout()
            } else {
                promotionTypeText.text = cellModel.data
                promotionTypeText.visibility = View.VISIBLE
                menuView.visibility = View.GONE
                promotionTypeText.requestLayout()
            }
        } else {
            promotionTypeText.visibility = View.VISIBLE
            menuView.visibility = View.GONE
            promotionTypeText.requestLayout()
        }
    }

    override fun setSelected(selectionState: SelectionState) {
        super.setSelected(selectionState)
    }
}