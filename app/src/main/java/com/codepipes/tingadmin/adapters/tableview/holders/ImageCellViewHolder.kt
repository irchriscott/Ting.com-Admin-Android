package com.codepipes.tingadmin.adapters.tableview.holders

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ImageCellViewHolder (private val view: View) : AbstractViewHolder(view) {

    private var imageView: CircleImageView = view.findViewById(R.id.image_view)
    private var cellContainer: LinearLayout = view.findViewById(R.id.cell_container)

    public fun setCellModel(cellModel: CellModel, columnPosition: Int) {
        Picasso.get().load(cellModel.data).into(imageView)
        cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        imageView.requestLayout()
    }

    override fun setSelected(selectionState: SelectionState) {
        super.setSelected(selectionState)
    }
}