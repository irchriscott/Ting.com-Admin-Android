package com.codepipes.tingadmin.fragments.sidebar


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.category.CategoryTableViewAdapter
import com.codepipes.tingadmin.dialogs.category.AddCategoryDialog
import com.codepipes.tingadmin.events.CategoriesTableViewListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.FoodCategory
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_categories.view.*
import kotlinx.android.synthetic.main.fragment_categories.view.empty_data
import kotlinx.android.synthetic.main.fragment_categories.view.progress_loader
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception


class CategoriesFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        if(!session.permissions.contains("can_add_category")){
            view.button_add_new_category.isClickable = false
            view.button_add_new_category.visibility = View.GONE
        }

        view.button_add_new_category.setOnClickListener {
            val addCategoryDialog = AddCategoryDialog()
            addCategoryDialog.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    activity?.runOnUiThread {
                        addCategoryDialog.dismiss()
                        loadCategories(view)
                    }
                }
                override fun onCancel() { addCategoryDialog.dismiss() }
            })
            addCategoryDialog.show(fragmentManager!!, addCategoryDialog.tag)
        }

        loadCategories(view)

        return view
    }

    @SuppressLint("DefaultLocale")
    private fun loadCategories(view: View) {

        val gson = Gson()

        TingClient.getRequest(Routes.categoriesAll, null, session.token) { _, isSuccess, result ->

            activity?.runOnUiThread {

                view.progress_loader.visibility = View.GONE

                if(isSuccess) {

                    try {

                        view.categories_table_view.visibility = View.VISIBLE
                        view.empty_data.visibility = View.GONE

                        val categories =
                            gson.fromJson<List<FoodCategory>>(result, object : TypeToken<List<FoodCategory>>(){}.type)

                        val categoryTableViewAdapter = CategoryTableViewAdapter(context!!)
                        view.categories_table_view.adapter = categoryTableViewAdapter
                        categoryTableViewAdapter.setCategoriesList(categories)
                        view.categories_table_view.tableViewListener =
                            CategoriesTableViewListener(
                                view.categories_table_view,
                                categories.toMutableList(),
                                context!!, fragmentManager!!,
                                object : DataUpdatedListener {
                                    override fun onDataUpdated() { activity?.runOnUiThread { loadCategories(view) } }
                                }, activity!! )

                    } catch (e: Exception) {

                        view.categories_table_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE

                        view.empty_data.empty_image.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_exclamation_white))

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.categories_table_view.visibility = View.GONE
                    view.empty_data.visibility = View.VISIBLE
                    view.empty_data.empty_image.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data.empty_text.text = result.capitalize()
                }
            }
        }
    }
}
