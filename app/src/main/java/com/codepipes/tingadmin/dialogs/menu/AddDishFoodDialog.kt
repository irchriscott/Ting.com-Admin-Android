package com.codepipes.tingadmin.dialogs.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.menu.dish.MenuDishFoodAdapter
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.DishFood
import com.codepipes.tingadmin.models.Menu
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_menu_add_food.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddDishFoodDialog : DialogFragment() {

    private var formDialogListener: FormDialogListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    private lateinit var foods: MutableList<Menu>
    private lateinit var dishFoods: List<DishFood>

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_menu_add_food, null, false)

        val session = UserAuthentication(context!!).get()

        val menu = Gson().fromJson(arguments?.getString(Constants.MENU_KEY), Menu::class.java)
        val menuDishFoodAdapter = MenuDishFoodAdapter(foods, dishFoods)

        Log.i("TING_DISH_FOOD", dishFoods.joinToString(", ") { "${it.food}-${it.quantity}" })

        view.menu_foods_recycler_view.layoutManager = LinearLayoutManager(context)
        view.menu_foods_recycler_view.adapter = menuDishFoodAdapter

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        view.dialog_button_save.setOnClickListener {

            val url = Routes.menusDishFoodUpdate.format(menu.id)
            val quantities = menuDishFoodAdapter.getQuantities().toSortedMap()
            val foods = menuDishFoodAdapter.getSelectedFoods().sortedBy { it.split("-")[0].toInt() }

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager!!, progressOverlay.tag)

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            for ((key, value) in quantities) {
                requestBodyBuilder.addFormDataPart("quantity", value.toString())
            }

            foods.forEach { requestBodyBuilder.addFormDataPart("food", it) }

            val requestBody = requestBodyBuilder.build()

            val request = Request.Builder()
                .header("Authorization", session!!.token)
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        TingToast(context!!, e.message!!, TingToastType.ERROR).showToast(Toast.LENGTH_LONG)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body!!.string()
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        try {
                            val serverResponse = Gson().fromJson(responseBody, ServerResponse::class.java)
                            TingToast(context!!, serverResponse.message, if(serverResponse.type == "success") { TingToastType.SUCCESS } else { TingToastType.ERROR }).showToast(
                                Toast.LENGTH_LONG)
                            if(serverResponse.type == "success") {
                                if(formDialogListener != null) { formDialogListener?.onSave()
                                } else {
                                    TingToast(context!!, "State Changed. Please, Try Again", TingToastType.DEFAULT).showToast(
                                        Toast.LENGTH_LONG)
                                    dialog?.dismiss()
                                }
                            }
                        } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(
                            Toast.LENGTH_LONG) }
                    }
                }
            })
        }

        return view
    }

    public fun setFoods(foods: MutableList<Menu>){
        this.foods = foods
    }

    public fun setDishFoods(foods: List<DishFood>){
        this.dishFoods = foods
    }

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }
}