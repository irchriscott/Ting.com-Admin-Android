package com.codepipes.tingadmin.fragments.sidebar.menus


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.menu.food.MenuFoodTableViewAdapter
import com.codepipes.tingadmin.dialogs.menu.AddMenuDialog
import com.codepipes.tingadmin.events.menu.MenuFoodTableViewListener
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
import kotlinx.android.synthetic.main.fragment_menu_food.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception


class MenuFoodFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_menu_food, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        view.button_add_new_menu_food.setOnClickListener {
            val addMenuDialog = AddMenuDialog()
            val bundle = Bundle()
            bundle.putInt(Constants.MENU_KEY, 1)
            addMenuDialog.arguments = bundle
            addMenuDialog.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    activity?.runOnUiThread {
                        addMenuDialog.dismiss()
                        loadMenusFood(view)
                    }
                }
                override fun onCancel() { addMenuDialog.dismiss() }
            })
            addMenuDialog.show(fragmentManager!!, addMenuDialog.tag)
        }

        loadMenusFood(view)

        return view
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun loadMenusFood(view: View) {
        val gson = Gson()
        TingClient.getRequest(Routes.menusFoodAll, null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                view.progress_loader.visibility = View.GONE

                if(isSuccess) {

                    try {
                        val menus =
                            gson.fromJson<List<Menu>>(result, object : TypeToken<List<Menu>>(){}.type)

                        if(menus.isNotEmpty()) {
                            view.menu_foods_table_view.visibility = View.VISIBLE
                            view.empty_data.visibility = View.GONE

                            val menuFoodTableViewAdapter = MenuFoodTableViewAdapter(context!!)
                            view.menu_foods_table_view.adapter = menuFoodTableViewAdapter
                            menuFoodTableViewAdapter.setMenuFoodList(menus)
                            view.menu_foods_table_view.tableViewListener =
                                MenuFoodTableViewListener(
                                    view.menu_foods_table_view,
                                    menus.toMutableList(),
                                    context!!, fragmentManager!!,
                                    object : DataUpdatedListener {
                                        override fun onDataUpdated() { activity?.runOnUiThread { loadMenusFood(view) } }
                                    }, activity!! )
                        } else {
                            view.menu_foods_table_view.visibility = View.GONE
                            view.empty_data.visibility = View.VISIBLE

                            view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_spoon_gray))
                            view.empty_data.empty_text.text = "No Menus Food To Show"
                        }

                    } catch (e: Exception) {

                        view.menu_foods_table_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE
                        view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.menu_foods_table_view.visibility = View.GONE
                    view.empty_data.visibility = View.VISIBLE
                    view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data.empty_text.text = result.capitalize()
                }
            }
        }
    }
}
