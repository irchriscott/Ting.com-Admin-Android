package com.codepipes.tingadmin.adapters.reservation

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.Booking
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions

class ReservationTableViewModel {

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
        list.add(ColumnHeaderModel("Date/Time"))
        list.add(ColumnHeaderModel("People"))
        list.add(ColumnHeaderModel("Table"))
        list.add(ColumnHeaderModel("Status"))
        list.add(ColumnHeaderModel("Created At"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(bookings: List<Booking>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in bookings.indices) {
            val booking = bookings[i]
            val list: MutableList<CellModel> = ArrayList()

            list.add(CellModel("1-bkg-$i", "${i + 1}"))
            list.add(CellModel("2-bkg-$i", booking.user.imageURL()))
            list.add(CellModel("3-bkg-$i", booking.user.name))
            list.add(CellModel("4-bkg-$i", UtilsFunctions.formatDate("${booking.date} ${booking.time}", true)))
            list.add(CellModel("5-bkg-$i", "${booking.people} people"))
            list.add(CellModel("6-bkg-$i", if(booking.table != null) { booking.table.number.toUpperCase() } else { Constants.TABLE_LOCATION[booking.location]!! }))
            list.add(CellModel("7-bkg-$i", Constants.BOOKING_STATUSES[booking.status]!!))
            list.add(CellModel("8-bkg-$i", UtilsFunctions.formatDate(booking.createdAt)))
            list.add(CellModel("9-bkg-$i", booking.id.toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(bookings: List<Booking>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(bookings)
        rowHeaderModelList = createRowHeaderList(bookings.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                1 -> Constants.IMAGE_TABLE_VIEW_CELL
                8 -> Constants.ACTIONS_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }
}