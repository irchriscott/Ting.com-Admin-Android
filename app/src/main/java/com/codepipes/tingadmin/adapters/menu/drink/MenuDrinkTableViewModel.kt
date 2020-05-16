package com.codepipes.tingadmin.adapters.menu.drink

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.Menu
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import java.text.NumberFormat

class MenuDrinkTableViewModel {

    var columnHeaderModeList: List<ColumnHeaderModel>? = null
        private set
    var rowHeaderModelList: List<RowHeaderModel>? = null
        private set
    var cellModelList: List<List<CellModel>>? = null
        private set

    private fun createColumnHeaderModelList(): List<ColumnHeaderModel> {

        val list: MutableList<ColumnHeaderModel> = arrayListOf<ColumnHeaderModel>()

        list.add(ColumnHeaderModel("NO"))
        list.add(ColumnHeaderModel("Image"))
        list.add(ColumnHeaderModel("Name"))
        list.add(ColumnHeaderModel("Price"))
        list.add(ColumnHeaderModel("Type"))
        list.add(ColumnHeaderModel("Is Available"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(menus: List<Menu>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in menus.indices) {
            val menu = menus[i]
            val list: MutableList<CellModel> = ArrayList()

            val price = if(menu.isCountable) {
                "${NumberFormat.getNumberInstance().format(menu.price)} ${menu.currency.toUpperCase()} / ${menu.quantity} Pieces"
            } else { "${NumberFormat.getNumberInstance().format(menu.price)} ${menu.currency}".toUpperCase() }

            list.add(CellModel("1-menu-$i", "${i + 1}"))
            list.add(CellModel("2-menu-$i", "${Routes.HOST_END_POINT}${menu.images.images[0].image}"))
            list.add(CellModel("3-menu-$i", menu.name))
            list.add(CellModel("4-menu-$i", price))
            list.add(CellModel("5-menu-$i", Constants.DRINK_TYPE[menu.drinkType]!!))
            list.add(CellModel("6-menu-$i", if(menu.isAvailable) { "YES" } else { "NO" }))
            list.add(CellModel("7-menu-$i", menu.id.toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(menus: List<Menu>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(menus)
        rowHeaderModelList = createRowHeaderList(menus.size)
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