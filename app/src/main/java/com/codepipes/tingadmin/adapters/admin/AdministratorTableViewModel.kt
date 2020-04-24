package com.codepipes.tingadmin.adapters.admin

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes

class AdministratorTableViewModel {

    var columnHeaderModeList: List<ColumnHeaderModel>? = null
        private set
    var rowHeaderModelList: List<RowHeaderModel>? = null
        private set
    var cellModelList: List<List<CellModel>>? = null
        private set

    private fun createColumnHeaderModelList(): List<ColumnHeaderModel> {

        val list: MutableList<ColumnHeaderModel> = arrayListOf<ColumnHeaderModel>()

        list.add(ColumnHeaderModel("Badge"))
        list.add(ColumnHeaderModel("Image"))
        list.add(ColumnHeaderModel("Name"))
        list.add(ColumnHeaderModel("Branch"))
        list.add(ColumnHeaderModel("Email"))
        list.add(ColumnHeaderModel("Type"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(admins: List<Administrator>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in admins.indices) {
            val admin = admins[i]
            val list: MutableList<CellModel> = ArrayList()

            list.add(CellModel("1-admin-$i", admin.badgeNumber))
            list.add(CellModel("2-admin-$i", "${Routes.HOST_END_POINT}${admin.image}"))
            list.add(CellModel("3-admin-$i", admin.name))
            list.add(CellModel("4-admin-$i", admin.branch.name))
            list.add(CellModel("5-admin-$i", admin.email))
            list.add(CellModel("6-admin-$i", Constants.ADMIN_TYPE[admin.type.toInt()]!!))
            list.add(CellModel("7-admin-$i", admin.token))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(admins: List<Administrator>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(admins)
        rowHeaderModelList = createRowHeaderList(admins.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                1 -> Constants.IMAGE_TABLE_VIEW_CELL
                6 -> Constants.ACTIONS_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }
}