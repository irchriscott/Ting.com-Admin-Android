package com.codepipes.tingadmin.fragments.sidebar.menus


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.menu.drink.MenuDrinkTableViewAdapter
import com.codepipes.tingadmin.dialogs.menu.AddMenuDialog
import com.codepipes.tingadmin.events.menu.MenuDrinkTableViewListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Menu
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_menu_drink.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception


class MenuDrinkFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_menu_drink, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        view.button_add_new_menu_drink.setOnClickListener {
            val addMenuDialog = AddMenuDialog()
            val bundle = Bundle()
            bundle.putInt(Constants.MENU_KEY, 2)
            addMenuDialog.arguments = bundle
            addMenuDialog.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    activity?.runOnUiThread {
                        addMenuDialog.dismiss()
                        loadMenusDrink(view)
                    }
                }
                override fun onCancel() { addMenuDialog.dismiss() }
            })
            addMenuDialog.show(fragmentManager!!, addMenuDialog.tag)
        }

        loadMenusDrink(view)

        return view
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun loadMenusDrink(view: View) {
        val gson = Gson()
        TingClient.getRequest(Routes.menusDrinkAll, null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {

                view.progress_loader.visibility = View.GONE

                if(isSuccess) {

                    try {
                        val menus =
                            gson.fromJson<List<Menu>>(result, object : TypeToken<List<Menu>>(){}.type)

                        if(menus.isNotEmpty()) {
                            view.menu_drinks_table_view.visibility = View.VISIBLE
                            view.empty_data.visibility = View.GONE

                            val menuDrinkTableViewAdapter = MenuDrinkTableViewAdapter(context!!)
                            view.menu_drinks_table_view.adapter = menuDrinkTableViewAdapter
                            menuDrinkTableViewAdapter.setMenuDrinkList(menus)
                            view.menu_drinks_table_view.tableViewListener =
                                MenuDrinkTableViewListener(
                                    view.menu_drinks_table_view,
                                    menus.toMutableList(),
                                    context!!, fragmentManager!!,
                                    object : DataUpdatedListener {
                                        override fun onDataUpdated() { activity?.runOnUiThread { loadMenusDrink(view) } }
                                    }, activity!! )
                        } else {
                            view.menu_drinks_table_view.visibility = View.GONE
                            view.empty_data.visibility = View.VISIBLE

                            view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_glass_gray))
                            view.empty_data.empty_text.text = "No Menus Drink To Show"
                        }

                    } catch (e: Exception) {

                        view.menu_drinks_table_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE
                        view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))

                        e.printStackTrace()

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                            e.printStackTrace()
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.menu_drinks_table_view.visibility = View.GONE
                    view.empty_data.visibility = View.VISIBLE
                    view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data.empty_text.text = result.capitalize()
                }
            }
        }
    }
}
