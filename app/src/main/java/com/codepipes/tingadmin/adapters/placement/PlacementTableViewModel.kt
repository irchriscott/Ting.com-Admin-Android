package com.codepipes.tingadmin.adapters.placement

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.Placement
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes

class PlacementTableViewModel {

    var columnHeaderModeList: List<ColumnHeaderModel>? = null
        private set
    var rowHeaderModelList: List<RowHeaderModel>? = null
        private set
    var cellModelList: List<List<CellModel>>? = null
        private set

    private fun createColumnHeaderModelList(): List<ColumnHeaderModel> {

        val list: MutableList<ColumnHeaderModel> = arrayListOf<ColumnHeaderModel>()

        list.add(ColumnHeaderModel("ID"))
        list.add(ColumnHeaderModel("Image"))
        list.add(ColumnHeaderModel("Name"))
        list.add(ColumnHeaderModel("Table"))
        list.add(ColumnHeaderModel("People"))
        list.add(ColumnHeaderModel("Bill No"))
        list.add(ColumnHeaderModel("Waiter"))
        list.add(ColumnHeaderModel("Action"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(placements: List<Placement>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in placements.indices) {
            val placement = placements[i]
            val list: MutableList<CellModel> = ArrayList()

            list.add(CellModel("1-place-$i", "${i + 1}"))
            list.add(CellModel("2-place-$i", placement.user.imageURL()))
            list.add(CellModel("3-place-$i", placement.user.name))
            list.add(CellModel("4-place-$i", placement.table.number))
            list.add(CellModel("5-place-$i", placement.people.toString()))
            list.add(CellModel("6-place-$i", placement.billNumber ?: "-"))
            list.add(CellModel("7-place-$i", if(placement.waiter != null) { "${placement.waiter.image}::${placement.waiter.name}" } else { "" }))
            list.add(CellModel("8-place-$i", placement.id.toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(placements: List<Placement>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(placements)
        rowHeaderModelList = createRowHeaderList(placements.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                1 -> Constants.IMAGE_TABLE_VIEW_CELL
                6 -> Constants.WAITER_TABLE_VIEW_CELL
                7 -> Constants.ACTIONS_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }
}