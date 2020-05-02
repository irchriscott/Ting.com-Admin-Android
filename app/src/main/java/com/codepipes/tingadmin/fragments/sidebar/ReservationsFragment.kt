package com.codepipes.tingadmin.fragments.sidebar


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.reservation.ReservationTableViewAdapter
import com.codepipes.tingadmin.events.DateReservationsTableViewListener
import com.codepipes.tingadmin.events.NewReservationsTableViewListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Booking
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_reservations.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import okhttp3.Interceptor
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class ReservationsFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_reservations, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        val calendar = Calendar.getInstance()

        val date = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = "yyyy-MM-dd"
            val sdf = SimpleDateFormat(format, Locale.US)
            view.reservation_date.setText(sdf.format(calendar.time))
            loadDateReservation(view, sdf.format(calendar.time))
        }

        view.reservation_date.setOnClickListener {
            DatePickerDialog(activity!!,
                R.style.DatePickerAppTheme, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        view.reservation_date.setText(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis())))

        loadDateReservation(view, null)
        loadNewReservation(view)

        return view
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun loadDateReservation(view: View, date: String?) {

        val gson = Gson()

        val interceptor = Interceptor {
            val url = it.request().url.newBuilder()
                .addQueryParameter("date", date)
                .build()
            val request = it.request().newBuilder()
                .header("Authorization", session.token)
                .url(url)
                .build()
            it.proceed(request)
        }

        TingClient.getRequest(Routes.reservationsDate, interceptor, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                view.progress_loader_date.visibility = View.GONE
                if(isSuccess) {
                    try {

                        val bookings =
                            gson.fromJson<List<Booking>>(result, object : TypeToken<List<Booking>>(){}.type)

                        if(bookings.isNotEmpty()) {
                            view.reservation_of_date_table_view.visibility = View.VISIBLE
                            view.empty_data_date.visibility = View.GONE

                            val reservationTableViewAdapter = ReservationTableViewAdapter(context!!)
                            view.reservation_of_date_table_view.adapter = reservationTableViewAdapter
                            reservationTableViewAdapter.setReservationsList(bookings)
                            view.reservation_of_date_table_view.tableViewListener =
                                DateReservationsTableViewListener(
                                    view.reservation_of_date_table_view,
                                    bookings.toMutableList(),
                                    context!!, fragmentManager!!,
                                    object : DataUpdatedListener {
                                        override fun onDataUpdated() { activity?.runOnUiThread { loadDateReservation(view, date) } }
                                    }, activity!! )
                        } else {
                            view.reservation_of_date_table_view.visibility = View.GONE
                            view.empty_data_date.visibility = View.VISIBLE

                            view.empty_data_date.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_navigation_reservations))
                            view.empty_data_date.empty_text.text = "No Reservation To Show"
                        }

                    } catch (e: Exception) {

                        view.reservation_of_date_table_view.visibility = View.GONE
                        view.empty_data_date.visibility = View.VISIBLE
                        view.empty_data_date.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))

                        e.printStackTrace()

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data_date.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data_date.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.reservation_of_date_table_view.visibility = View.GONE
                    view.empty_data_date.visibility = View.VISIBLE
                    view.empty_data_date.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data_date.empty_text.text = result.capitalize()
                }
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun loadNewReservation(view: View) {

        val gson = Gson()

        TingClient.getRequest(Routes.reservationsNew, null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                view.progress_loader_new.visibility = View.GONE
                if(isSuccess) {

                    try {

                        val bookings =
                            gson.fromJson<List<Booking>>(result, object : TypeToken<List<Booking>>(){}.type)

                        if(bookings.isNotEmpty()) {
                            view.new_reservation_table_view.visibility = View.VISIBLE
                            view.empty_data_new.visibility = View.GONE

                            val reservationTableViewAdapter = ReservationTableViewAdapter(context!!)
                            view.new_reservation_table_view.adapter = reservationTableViewAdapter
                            reservationTableViewAdapter.setReservationsList(bookings)
                            view.new_reservation_table_view.tableViewListener =
                                NewReservationsTableViewListener(
                                    view.new_reservation_table_view,
                                    bookings.toMutableList(),
                                    context!!, fragmentManager!!,
                                    object : DataUpdatedListener {
                                        override fun onDataUpdated() {
                                            activity?.runOnUiThread {
                                                loadNewReservation(view)
                                                loadDateReservation(view, null)
                                            }
                                        }
                                    }, activity!! )
                        } else {
                            view.new_reservation_table_view.visibility = View.GONE
                            view.empty_data_new.visibility = View.VISIBLE

                            view.empty_data_new.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_navigation_reservations))
                            view.empty_data_new.empty_text.text = "No Reservation To Show"
                        }

                    } catch (e: Exception) {

                        view.new_reservation_table_view.visibility = View.GONE
                        view.empty_data_new.visibility = View.VISIBLE
                        view.empty_data_new.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data_new.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data_new.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.new_reservation_table_view.visibility = View.GONE
                    view.empty_data_new.visibility = View.VISIBLE
                    view.empty_data_new.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data_new.empty_text.text = result.capitalize()
                }
            }
        }
    }
}
