package com.codepipes.tingadmin.fragments.sidebar


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.table.TableTableViewAdapter
import com.codepipes.tingadmin.events.TablesTableViewListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.RestaurantTable
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_tables.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception


class TablesFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_tables, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        loadTables(view)

        return view
    }

    @SuppressLint("DefaultLocale")
    private fun loadTables(view: View) {

        val gson = Gson()

        TingClient.getRequest(Routes.tablesAll, null, session.token) { _, isSuccess, result ->

            activity?.runOnUiThread {

                view.progress_loader.visibility = View.GONE

                if(isSuccess) {

                    try {

                        view.tables_table_view.visibility = View.VISIBLE
                        view.empty_data.visibility = View.GONE

                        val tables =
                            gson.fromJson<List<RestaurantTable>>(result, object : TypeToken<List<RestaurantTable>>(){}.type)

                        val tableTableViewAdapter = TableTableViewAdapter(context!!)
                        view.tables_table_view.adapter = tableTableViewAdapter
                        tableTableViewAdapter.setTablesList(tables)
                        view.tables_table_view.tableViewListener =
                            TablesTableViewListener(
                                view.tables_table_view,
                                tables.toMutableList(),
                                context!!, fragmentManager!!,
                                object : DataUpdatedListener {
                                    override fun onDataUpdated() { activity?.runOnUiThread { loadTables(view) } }
                                }, activity!! )

                    } catch (e: Exception) {

                        view.tables_table_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE

                        view.empty_data.empty_image.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_exclamation_white))

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.tables_table_view.visibility = View.GONE
                    view.empty_data.visibility = View.VISIBLE
                    view.empty_data.empty_image.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data.empty_text.text = result.capitalize()
                }
            }
        }
    }
}
