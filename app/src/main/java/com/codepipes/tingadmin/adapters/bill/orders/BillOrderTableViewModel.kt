package com.codepipes.tingadmin.adapters.bill.orders

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.Order
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import java.text.NumberFormat

class BillOrderTableViewModel {

    var columnHeaderModeList: List<ColumnHeaderModel>? = null
        private set
    var rowHeaderModelList: List<RowHeaderModel>? = null
        private set
    var cellModelList: List<List<CellModel>>? = null
        private set

    private fun createColumnHeaderModelList(): List<ColumnHeaderModel> {

        val list: MutableList<ColumnHeaderModel> = arrayListOf<ColumnHeaderModel>()

        list.add(ColumnHeaderModel("No"))
        list.add(ColumnHeaderModel("Image"))
        list.add(ColumnHeaderModel("Name"))
        list.add(ColumnHeaderModel("Price"))
        list.add(ColumnHeaderModel("Quantity"))
        list.add(ColumnHeaderModel("Total"))
        list.add(ColumnHeaderModel("Has Promotion"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(orders: List<Order>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in orders.indices) {
            val order = orders[i]
            val list: MutableList<CellModel> = ArrayList()

            list.add(CellModel("1-order-$i", "${i + 1}"))
            list.add(CellModel("2-order-$i", "${Routes.HOST_END_POINT}${order.menu.menu.images.images[0].image}"))
            list.add(CellModel("3-order-$i", order.menu.menu.name))
            list.add(CellModel("4-order-$i", "${order.menu.menu.currency} ${NumberFormat.getNumberInstance().format(order.price)}"))
            list.add(CellModel("5-order-$i", order.quantity.toString()))
            list.add(CellModel("6-order-$i", "${order.menu.menu.currency} ${NumberFormat.getNumberInstance().format(order.price * order.quantity)}"))
            list.add(CellModel("7-order-$i", if(order.hasPromotion){ "YES" } else { "NO" }))
            list.add(CellModel("8-order-$i", (!order.isDeclined && !order.isDelivered).toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(orders: List<Order>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(orders)
        rowHeaderModelList = createRowHeaderList(orders.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                1 -> Constants.IMAGE_TABLE_VIEW_CELL
                7 -> Constants.BILL_ORDER_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }
}