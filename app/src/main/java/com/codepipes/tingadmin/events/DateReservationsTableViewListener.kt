package com.codepipes.tingadmin.events

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.custom.ActionSheet
import com.codepipes.tingadmin.dialogs.reservation.AcceptReservationDialog
import com.codepipes.tingadmin.dialogs.reservation.DeclineReservationDialog
import com.codepipes.tingadmin.dialogs.reservation.LoadReservationDialog
import com.codepipes.tingadmin.interfaces.ActionSheetCallBack
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Booking
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.tableview.ITableView
import com.codepipes.tingadmin.tableview.listener.ITableViewListener
import com.codepipes.tingadmin.utils.Constants
import com.google.gson.Gson

class DateReservationsTableViewListener (

    private val mTableView: ITableView,
    private val bookings: MutableList<Booking>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val dataUpdatedListener: DataUpdatedListener,
    private val activity: FragmentActivity

) : ITableViewListener {

    private val gson = Gson()
    private val session = UserAuthentication(context).get()!!

    private val reservationMenu = mutableMapOf<Int, String>(0 to "Accept", 1 to "Decline", 2 to "Payment", 3 to "Refund")

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showBookingMenu(column, row)
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        if(column == 8) {
            showBookingMenu(column, row)
        } else {
            val booking = bookings[row]

            val bookingBundle = Bundle()
            bookingBundle.putString(Constants.BOOKING_KEY, Gson().toJson(booking))

            val loadReservationDialog = LoadReservationDialog()
            loadReservationDialog.arguments = bookingBundle
            loadReservationDialog.show(fragmentManager, loadReservationDialog.tag)
        }
    }

    override fun onColumnHeaderDoubleClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {}

    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        showBookingMenu(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {}

    private fun showBookingMenu(column: Int, row: Int) {

        val booking = bookings[row]
        val menuList = mutableListOf<String>()

        if(session.permissions.contains("can_accept_booking")) {
            if(booking.status == 2) { menuList.add(reservationMenu[0]!!) }
            if(booking.status == 3) { menuList.add(reservationMenu[2]!!) }
        }

        if(session.permissions.contains("can_cancel_booking")) {
            if(arrayListOf<Int>(3, 4).contains(booking.status)) {
                menuList.add(reservationMenu[1]!!)
            }
            if(booking.status == 7) { menuList.add(reservationMenu[3]!!) }
        }

        val bookingBundle = Bundle()
        bookingBundle.putString(Constants.BOOKING_KEY, Gson().toJson(booking))

        if(booking.status != 5 && booking.status != 6) {
            val actionSheet = ActionSheet(context, menuList)
                .setTitle("Options")
                .setColorData(context.resources.getColor(R.color.colorGray))
                .setColorTitleCancel(context.resources.getColor(R.color.colorGoogleRedTwo))
                .setColorSelected(context.resources.getColor(R.color.colorPrimary))
                .setCancelTitle("Cancel")

            actionSheet.create(object : ActionSheetCallBack {

                @SuppressLint("DefaultLocale")
                override fun data(data: String, position: Int) {
                    when(data) {
                        reservationMenu[0] -> {
                            val acceptReservationDialog = AcceptReservationDialog()
                            acceptReservationDialog.arguments = bookingBundle
                            acceptReservationDialog.setFormDialogListener(object : FormDialogListener {
                                override fun onSave() {
                                    acceptReservationDialog.dismiss()
                                    dataUpdatedListener.onDataUpdated()
                                }
                                override fun onCancel() { acceptReservationDialog.dismiss() }
                            })
                            acceptReservationDialog.show(fragmentManager, acceptReservationDialog.tag)
                        }
                        reservationMenu[1] -> {
                            val declineReservationDialog = DeclineReservationDialog()
                            declineReservationDialog.arguments = bookingBundle
                            declineReservationDialog.setFormDialogListener(object : FormDialogListener {
                                override fun onSave() {
                                    declineReservationDialog.dismiss()
                                    dataUpdatedListener.onDataUpdated()
                                }
                                override fun onCancel() { declineReservationDialog.dismiss() }
                            })
                            declineReservationDialog.show(fragmentManager, declineReservationDialog.tag)
                        }
                    }
                }
            })
        }
    }
}