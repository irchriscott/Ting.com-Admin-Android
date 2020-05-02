package com.codepipes.tingadmin.dialogs.reservation

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.models.Booking
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_booking_load.view.*

class LoadReservationDialog : DialogFragment() {

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_booking_load, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val booking = Gson().fromJson(arguments?.getString(Constants.BOOKING_KEY), Booking::class.java)
        view.user_name.text = booking.user.name
        view.user_address.text = "${booking.user.town}, ${booking.user.country}"
        view.user_email.text = booking.user.email
        view.user_phone_number.text = booking.user.phone
        view.reservation_date.text = UtilsFunctions.formatDate("${booking.date} ${booking.time}", true)
        view.reservation_people.text = "${booking.people} people"
        view.reservation_table.text = if(booking.table != null) { booking.table.number } else { Constants.TABLE_LOCATION[booking.location] }
        view.reservation_status.text = Constants.BOOKING_STATUSES[booking.status]
        view.reservation_status.setTextColor(
            when(booking.status) {
                1 -> resources.getColor(R.color.colorOrange)
                2, 7 -> resources.getColor(R.color.colorRed)
                else -> resources.getColor(R.color.colorGreen)
            }
        )
        view.reservation_status_image.setImageDrawable(
            when(booking.status) {
                1 -> resources.getDrawable(R.drawable.ic_hourglass_full_gray_24dp)
                2, 7 -> resources.getDrawable(R.drawable.ic_close_white_24dp)
                else -> resources.getDrawable(R.drawable.ic_check_white)
            }
        )
        view.reservation_status_image.setColorFilter(
            when(booking.status) {
                1 -> resources.getColor(R.color.colorOrange)
                2, 7 -> resources.getColor(R.color.colorRed)
                else -> resources.getColor(R.color.colorGreen)
            }
        )
        view.reservation_created_date.text = UtilsFunctions.formatDate(booking.updatedAt)
        Picasso.get().load(booking.user.imageURL()).into(view.user_image)

        view.dialog_button_close.setOnClickListener { dialog?.dismiss() }

        return view
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog?.window!!.setLayout(width, height)
        }
    }
}