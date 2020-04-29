package com.codepipes.tingadmin.adapters.branch

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.tableview.holders.ActionsCellViewHolder
import com.codepipes.tingadmin.adapters.tableview.holders.CellViewHolder
import com.codepipes.tingadmin.adapters.tableview.holders.ColumnHeaderViewHolder
import com.codepipes.tingadmin.adapters.tableview.holders.RowHeaderViewHolder
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.Branch
import com.codepipes.tingadmin.tableview.adapter.AbstractTableAdapter
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder
import com.codepipes.tingadmin.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.codepipes.tingadmin.utils.Constants

class BranchTableViewAdapter (val context: Context) : AbstractTableAdapter<ColumnHeaderModel?, RowHeaderModel?, CellModel?>() {

    private val tableTableViewModel: BranchTableViewModel = BranchTableViewModel()

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return when (viewType) {
            Constants.ACTIONS_TABLE_VIEW_CELL -> {
                val layout = LayoutInflater.from(context)
                    .inflate(R.layout.tableview_actions_cell_view, parent, false)
                ActionsCellViewHolder(layout)
            }
            else -> {
                val layout = LayoutInflater.from(context)
                    .inflate(R.layout.tableview_cell_view, parent, false)
                CellViewHolder(layout)
            }
        }
    }

    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractSorterViewHolder {
        val layout: View = LayoutInflater.from(context)
            .inflate(R.layout.tableview_column_header_view, parent, false)
        return ColumnHeaderViewHolder(layout, tableView)
    }

    override fun onCreateRowHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val layout: View =
            LayoutInflater.from(context).inflate(R.layout.tableview_row_header_view, parent, false)
        return RowHeaderViewHolder(layout)
    }

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder,
        cellItemModel: CellModel?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        val cell = cellItemModel as CellModel?
        when (holder) {
            is CellViewHolder -> {
                (holder as CellViewHolder).setCellModel(cell!!, columnPosition)
            }
            is ActionsCellViewHolder -> {
                (holder as ActionsCellViewHolder).setCellModel(cell!!, columnPosition)
            }
        }
    }

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder,
        columnHeaderItemModel: ColumnHeaderModel?,
        columnPosition: Int
    ) {
        val columnHeader = columnHeaderItemModel as ColumnHeaderModel?
        val columnHeaderViewHolder = holder as ColumnHeaderViewHolder
        columnHeaderViewHolder.setColumnHeaderModel(columnHeader!!, columnPosition)
    }

    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder,
        rowHeaderItemModel: RowHeaderModel?,
        rowPosition: Int
    ) {
        val rowHeaderModel = rowHeaderItemModel as RowHeaderModel
        val rowHeaderViewHolder = holder as RowHeaderViewHolder
        rowHeaderViewHolder.rowHeaderTextView.text = rowHeaderModel.data
    }

    override fun getCellItemViewType(position: Int): Int =
        BranchTableViewModel.getCellItemViewType(position)

    @SuppressLint("InflateParams")
    override fun onCreateCornerView(parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.tableview_corner_view, null, false);
    }

    override fun getColumnHeaderItemViewType(position: Int): Int = 0

    override fun getRowHeaderItemViewType(position: Int): Int = 0

    fun setBranchesList(branches: List<Branch>) {
        tableTableViewModel.generateListForTableView(branches)
        setAllItems(
            tableTableViewModel.columnHeaderModeList, tableTableViewModel
                .rowHeaderModelList, tableTableViewModel.cellModelList
        )
    }
}