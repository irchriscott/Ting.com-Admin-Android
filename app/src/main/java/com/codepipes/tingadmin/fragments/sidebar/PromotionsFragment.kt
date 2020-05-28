package com.codepipes.tingadmin.fragments.sidebar

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.promotion.PromotionTableViewAdapter
import com.codepipes.tingadmin.dialogs.promotion.AddPromotionDialog
import com.codepipes.tingadmin.events.PromotionsTableViewListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.MenuPromotion
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_promotions.view.*
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception


class PromotionsFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_promotions, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        if(!session.permissions.contains("can_add_promotion")){
            view.button_add_new_promotion.isClickable = false
            view.button_add_new_promotion.visibility = View.GONE
        }

        view.button_add_new_promotion.setOnClickListener {
            val addPromotionDialog = AddPromotionDialog()
            addPromotionDialog.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    activity?.runOnUiThread {
                        addPromotionDialog.dismiss()
                        loadPromotions(view)
                    }
                }
                override fun onCancel() { addPromotionDialog.dismiss() }
            })
            addPromotionDialog.show(fragmentManager!!, addPromotionDialog.tag)
        }

        loadPromotions(view)

        return view
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun loadPromotions(view: View) {
        val gson = Gson()
        TingClient.getRequest(Routes.promotionsAll, null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                view.progress_loader.visibility = View.GONE
                if(isSuccess) {
                    try {
                        val promotions =
                            gson.fromJson<List<MenuPromotion>>(result, object : TypeToken<List<MenuPromotion>>(){}.type)

                        if(promotions.isNotEmpty()){
                            view.promotions_table_view.visibility = View.VISIBLE
                            view.empty_data.visibility = View.GONE

                            val promotionTableViewAdapter = PromotionTableViewAdapter(context!!)
                            view.promotions_table_view.adapter = promotionTableViewAdapter
                            promotionTableViewAdapter.setPromotionsList(promotions)
                            view.promotions_table_view.tableViewListener =
                                PromotionsTableViewListener(
                                    view.promotions_table_view,
                                    promotions.toMutableList(),
                                    context!!, fragmentManager!!,
                                    object : DataUpdatedListener {
                                        override fun onDataUpdated() { activity?.runOnUiThread { loadPromotions(view) } }
                                    }, activity!! )
                        } else {
                            view.promotions_table_view.visibility = View.GONE
                            view.empty_data.visibility = View.VISIBLE

                            view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_star_filled_gray))
                            view.empty_data.empty_text.text = "No Promotion To Show"
                        }

                    } catch (e: Exception) {

                        view.promotions_table_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE
                        view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))

                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.promotions_table_view.visibility = View.GONE
                    view.empty_data.visibility = View.VISIBLE
                    view.empty_data.empty_image.setImageDrawable(resources.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data.empty_text.text = result.capitalize()
                }
            }
        }
    }
}
