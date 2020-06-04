package com.codepipes.tingadmin.adapters.bill.extras

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.BillExtra
import com.codepipes.tingadmin.utils.Constants
import java.text.NumberFormat

class BillExtraTableViewModel {

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
        list.add(ColumnHeaderModel("Price"))
        list.add(ColumnHeaderModel("Quantity"))
        list.add(ColumnHeaderModel("Total"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(extras: List<BillExtra>, currency: String): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in extras.indices) {
            val extra = extras[i]
            val list: MutableList<CellModel> = ArrayList()

            list.add(CellModel("1-extra-$i", "${i + 1}"))
            list.add(CellModel("2-extra-$i", extra.name))
            list.add(CellModel("3-extra-$i", "$currency ${NumberFormat.getNumberInstance().format(extra.price)}"))
            list.add(CellModel("4-extra-$i", extra.quantity.toString()))
            list.add(CellModel("5-extra-$i", "$currency ${NumberFormat.getNumberInstance().format(extra.total)}"))
            list.add(CellModel("6-extra-$i", extra.id.toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(extras: List<BillExtra>, currency: String) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(extras, currency)
        rowHeaderModelList = createRowHeaderList(extras.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                5 -> Constants.IMAGE_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }
}