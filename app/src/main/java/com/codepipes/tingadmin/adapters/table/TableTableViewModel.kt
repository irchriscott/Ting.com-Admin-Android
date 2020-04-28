package com.codepipes.tingadmin.adapters.table

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.RestaurantTable
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions

class TableTableViewModel {

    var columnHeaderModeList: List<ColumnHeaderModel>? = null
        private set
    var rowHeaderModelList: List<RowHeaderModel>? = null
        private set
    var cellModelList: List<List<CellModel>>? = null
        private set

    private fun createColumnHeaderModelList(): List<ColumnHeaderModel> {

        val list: MutableList<ColumnHeaderModel> = arrayListOf<ColumnHeaderModel>()

        list.add(ColumnHeaderModel("NO."))
        list.add(ColumnHeaderModel("Location"))
        list.add(ColumnHeaderModel("Chair Type"))
        list.add(ColumnHeaderModel("Max People"))
        list.add(ColumnHeaderModel("Available"))
        list.add(ColumnHeaderModel("Default Waiter"))
        list.add(ColumnHeaderModel("Created At"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(tables: List<RestaurantTable>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in tables.indices) {
            val table = tables[i]
            val list: MutableList<CellModel> = ArrayList()

            list.add(CellModel("1-table-$i", table.number))
            list.add(CellModel("2-table-$i", Constants.TABLE_LOCATION[table.location]!!))
            list.add(CellModel("3-table-$i", Constants.CHAIR_TYPE[table.chairType]!!))
            list.add(CellModel("4-table-$i", table.maxPeople.toString()))
            list.add(CellModel("5-table-$i", if(table.isAvailable) { "YES" } else { "NO" }))
            list.add(CellModel("6-table-$i", if(table.waiter != null) { "${table.waiter.image}::${table.waiter.name}" } else { "" }))
            list.add(CellModel("7-table-$i", UtilsFunctions.formatDate(table.createdAt)))
            list.add(CellModel("8-table-$i", table.id.toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(tables: List<RestaurantTable>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(tables)
        rowHeaderModelList = createRowHeaderList(tables.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                5 -> Constants.WAITER_TABLE_VIEW_CELL
                7 -> Constants.ACTIONS_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }
}