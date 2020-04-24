package com.codepipes.tingadmin.fragments.sidebar


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.admin.AdministratorTableViewAdapter
import com.codepipes.tingadmin.events.AdministratorsTableViewListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_administrators.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception


class AdministratorsFragment : Fragment() {

    private lateinit var session: Administrator
    private lateinit var userAuthentication: UserAuthentication

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_administrators, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        val gson = Gson()

        view.button_add_new_administrator.setOnClickListener {  }

        TingClient.getRequest(Routes.administratorsAll, null, session.token) { _, isSuccess, result ->

            activity?.runOnUiThread {

                view.progress_loader.visibility = View.GONE

                if(isSuccess) {

                    try {

                        view.administrators_table_view.visibility = View.VISIBLE
                        view.empty_data.visibility = View.GONE

                        val administrators =
                            gson.fromJson<List<Administrator>>(result, object : TypeToken<List<Administrator>>(){}.type)

                        val administratorTableViewAdapter = AdministratorTableViewAdapter(context!!)
                        view.administrators_table_view.adapter = administratorTableViewAdapter
                        administratorTableViewAdapter.setAdminsList(administrators)
                        view.administrators_table_view.tableViewListener =
                            AdministratorsTableViewListener(
                                view.administrators_table_view,
                                administrators.toMutableList(),
                                context!!, fragmentManager!!,
                                object : DataUpdatedListener {
                                    override fun onDataUpdated() {  }
                                }
                            )

                    } catch (e: Exception) {

                        view.administrators_table_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE

                        view.empty_data.empty_image.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_exclamation_white))

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.administrators_table_view.visibility = View.GONE
                    view.empty_data.visibility = View.VISIBLE
                    view.empty_data.empty_image.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data.empty_text.text = result.capitalize()
                }
            }
        }

        return view
    }
}
