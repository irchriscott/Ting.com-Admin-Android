package com.codepipes.tingadmin.adapters.branch

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.Branch
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.UtilsFunctions

class BranchTableViewModel {

    var columnHeaderModeList: List<ColumnHeaderModel>? = null
        private set
    var rowHeaderModelList: List<RowHeaderModel>? = null
        private set
    var cellModelList: List<List<CellModel>>? = null
        private set

    private fun createColumnHeaderModelList(): List<ColumnHeaderModel> {

        val list: MutableList<ColumnHeaderModel> = arrayListOf<ColumnHeaderModel>()

        list.add(ColumnHeaderModel("NO."))
        list.add(ColumnHeaderModel("Name"))
        list.add(ColumnHeaderModel("Country"))
        list.add(ColumnHeaderModel("Town"))
        list.add(ColumnHeaderModel("Is Available"))
        list.add(ColumnHeaderModel("Address"))
        list.add(ColumnHeaderModel("Email"))
        list.add(ColumnHeaderModel("Phone"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(branches: List<Branch>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in branches.indices) {
            val branch = branches[i]
            val list: MutableList<CellModel> = ArrayList()

            list.add(CellModel("1-branch-$i", "${i + 1}"))
            list.add(CellModel("2-branch-$i", branch.name))
            list.add(CellModel("3-branch-$i", branch.country))
            list.add(CellModel("4-branch-$i", branch.town))
            list.add(CellModel("5-branch-$i", if(branch.isAvailable) { "YES" } else { "NO" }))
            list.add(CellModel("6-branch-$i", branch.address))
            list.add(CellModel("7-branch-$i", branch.email))
            list.add(CellModel("8-branch-$i", branch.phone))
            list.add(CellModel("9-branch-$i", branch.id.toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(branches: List<Branch>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(branches)
        rowHeaderModelList = createRowHeaderList(branches.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                8 -> Constants.ACTIONS_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }
}