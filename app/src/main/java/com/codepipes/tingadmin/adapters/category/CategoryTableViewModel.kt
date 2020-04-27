package com.codepipes.tingadmin.adapters.category

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.FoodCategory
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions

class CategoryTableViewModel {

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
        list.add(ColumnHeaderModel("Description"))
        list.add(ColumnHeaderModel("Created At"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(categories: List<FoodCategory>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in categories.indices) {
            val category = categories[i]
            val list: MutableList<CellModel> = ArrayList()

            list.add(CellModel("1-cat-$i", "${i + 1}"))
            list.add(CellModel("2-cat-$i", "${Routes.HOST_END_POINT}${category.image}"))
            list.add(CellModel("3-cat-$i", category.name))
            list.add(CellModel("4-cat-$i", category.description))
            list.add(CellModel("5-cat-$i", UtilsFunctions.formatDate(category.createdAt)))
            list.add(CellModel("7-cat-$i", category.id.toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(categories: List<FoodCategory>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(categories)
        rowHeaderModelList = createRowHeaderList(categories.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                1 -> Constants.IMAGE_TABLE_VIEW_CELL
                5 -> Constants.ACTIONS_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }

}