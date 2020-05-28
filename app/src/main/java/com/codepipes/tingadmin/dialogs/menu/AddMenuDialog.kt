package com.codepipes.tingadmin.dialogs.menu

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.custom.MenuImageView
import com.codepipes.tingadmin.dialogs.messages.*
import com.codepipes.tingadmin.dialogs.utils.ImageSelectorDialog
import com.codepipes.tingadmin.dialogs.utils.LinkEditorDialog
import com.codepipes.tingadmin.interfaces.*
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.FoodCategory
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_menu_edit.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddMenuDialog : DialogFragment() {

    private var formDialogListener: FormDialogListener? = null

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    private var selectedCategory: Int? = null
    private var selectedCuisine: Int? = null
    private var selectedFoodType: Int? = null
    private var selectedDrinkType: Int? = null
    private var selectedDishType: Int? = null

    private val menuImages = mutableListOf<File>()

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_menu_edit, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        val type = arguments?.getInt(Constants.MENU_KEY, 1)

        val typeText = when (type) {
            1 -> "Food"
            2 -> "Drink"
            3 -> "Dish"
            else -> "Menu"
        }

        view.menu_for_all_branches_view.visibility = View.VISIBLE

        when(type) {
            1 -> {
                view.menu_food_group.visibility = View.VISIBLE
                view.menu_food_category_select.setOnClickListener { selectCategory(typeText, view) }
                view.menu_food_cuisine_select.setOnClickListener { selectCuisine(typeText, view) }
                view.menu_food_type_select.setOnClickListener {
                    val selectDialog = SelectDialog()
                    val selectBundle = Bundle()
                    selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select $typeText Type")
                    selectDialog.arguments = selectBundle
                    selectDialog.setItems(Constants.FOOD_TYPE.toSortedMap().map { it.value }, object : SelectItemListener {
                        override fun onSelectItem(position: Int) {
                            selectedFoodType = position + 1
                            view.selected_menu_food_type.text = Constants.FOOD_TYPE[selectedFoodType!!]
                            view.selected_menu_food_type.setTextColor(resources.getColor(R.color.colorGray))
                            selectDialog.dismiss()
                        }
                    })
                    selectDialog.show(fragmentManager!!, selectDialog.tag)
                }
            }
            2 -> {
                view.menu_drink_group.visibility = View.VISIBLE
                view.menu_drink_type_select.setOnClickListener {
                    val selectDialog = SelectDialog()
                    val selectBundle = Bundle()
                    selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select $typeText Type")
                    selectDialog.arguments = selectBundle
                    selectDialog.setItems(Constants.DRINK_TYPE.toSortedMap().map { it.value }, object : SelectItemListener {
                        override fun onSelectItem(position: Int) {
                            selectedDrinkType = position + 1
                            view.selected_menu_drink_type.text = Constants.DRINK_TYPE[selectedDrinkType!!]
                            view.selected_menu_drink_type.setTextColor(resources.getColor(R.color.colorGray))
                            selectDialog.dismiss()
                        }
                    })
                    selectDialog.show(fragmentManager!!, selectDialog.tag)
                }
            }
            3 -> {
                view.menu_dish_group.visibility = View.VISIBLE
                view.menu_dish_category_select.setOnClickListener { selectCategory(typeText, view) }
                view.menu_dish_cuisine_select.setOnClickListener { selectCuisine(typeText, view) }
                view.menu_dish_type_select.setOnClickListener {
                    val selectDialog = SelectDialog()
                    val selectBundle = Bundle()
                    selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select $typeText Type")
                    selectDialog.arguments = selectBundle
                    selectDialog.setItems(Constants.DISH_TIME.toSortedMap().map { it.value }, object : SelectItemListener {
                        override fun onSelectItem(position: Int) {
                            selectedDishType = position + 1
                            view.selected_menu_dish_type.text = Constants.DISH_TIME[selectedDishType!!]
                            view.selected_menu_dish_type.setTextColor(resources.getColor(R.color.colorGray))
                            selectDialog.dismiss()
                        }
                    })
                    selectDialog.show(fragmentManager!!, selectDialog.tag)
                }
            }
        }

        val session = UserAuthentication(context!!).get()

        view.dialog_title.text = "Add Menu $typeText"
        view.menu_name_label.text = "Enter $typeText Name :"
        view.menu_name.hint = "$typeText Name"
        view.menu_description_label.text = "Enter $typeText Description :"
        view.menu_description.hint = "$typeText Description :"
        view.menu_add_images_label.text = "Add $typeText Images"

        view.selected_menu_currency.text = Constants.CURRENCIES[session!!.branch.restaurant?.config?.currency]

        view.menu_ingredients_list.setPlaceholder("Ingredient List")
        view.menu_ingredients_list.setEditorHeight(180)
        view.menu_ingredients_list.setEditorFontSize(13)
        view.menu_ingredients_list.setEditorFontColor(resources.getColor(R.color.colorGray))

        view.action_undo.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.undo() })
        view.action_redo.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.redo() })
        view.action_bold.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setBold() })
        view.action_italic.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setItalic() })
        view.action_subscript.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setSubscript() })
        view.action_superscript.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setSuperscript() })
        view.action_strikethrough.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setStrikeThrough() })
        view.action_underline.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setUnderline() })
        view.action_indent.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setIndent() })
        view.action_outdent.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setOutdent() })
        view.action_align_left.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setAlignLeft() })
        view.action_align_center.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setAlignCenter() })
        view.action_align_right.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setAlignRight() })
        view.action_insert_bullets.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setBullets() })
        view.action_insert_numbers.setOnClickListener(View.OnClickListener { view.menu_ingredients_list.setNumbers() })
        view.action_insert_link.setOnClickListener(View.OnClickListener {
            val linkEditorDialog = LinkEditorDialog()
            linkEditorDialog.setOnLinkSet(object : EditorLinkListener {
                override fun onLinkSet(link: String, title: String) {
                    if(link.isNotBlank() && link.isNotEmpty()){
                        view.menu_ingredients_list.insertLink(link, if(title.isNotBlank() && title.isNotEmpty()){ title } else { link })
                        linkEditorDialog.dismiss()
                    } else { TingToast(context!!, "Please, Insert Link URL", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                }
            })
            linkEditorDialog.show(fragmentManager!!, linkEditorDialog.tag)
        })

        view.menu_add_images.setOnClickListener {
            val imageSelectDialog = ImageSelectorDialog()
            imageSelectDialog.setMaxImages(4)
            imageSelectDialog.setSelectorType(2)
            imageSelectDialog.setOnImageSelectorListener(object : ImageSelectorListener {
                override fun onMultipleImagesSelected(images: List<String>) {
                    if(images.isNotEmpty()) {
                        images.forEach {
                            try {
                                val image = File(it)
                                menuImages.add(image)
                                val menuImageView = MenuImageView(context!!)
                                menuImageView.setImage(image)
                                menuImageView.setOnDeleteImage(object : DeleteImageListener {
                                    override fun onDeleteImage() {
                                        val confirmDialog = ConfirmDialog()
                                        val bundle = Bundle()
                                        bundle.putString(Constants.CONFIRM_TITLE_KEY, "Remove Image")
                                        bundle.putString(Constants.CONFIRM_MESSAGE_KEY, "Do you really want to remove this image ?")
                                        confirmDialog.arguments = bundle
                                        confirmDialog.onDialogListener(object :
                                            ConfirmDialogListener {
                                            override fun onAccept() {
                                                confirmDialog.dismiss()
                                                menuImages.remove(image)
                                                menuImageView.visibility = View.GONE
                                            }
                                            override fun onCancel() { confirmDialog.dismiss() }
                                        })
                                        confirmDialog.show(fragmentManager!!, confirmDialog.tag)
                                    }
                                })
                                view.menu_images.addView(menuImageView)
                            } catch (e: java.lang.Exception){}
                        }
                        imageSelectDialog.dismiss()
                    } else { TingToast(context!!, "Image Cannot Be Null", TingToastType.DEFAULT).showToast(
                        Toast.LENGTH_LONG) }
                }
                override fun onSingleImageSelected(image: String) {}
                override fun onCancel() { imageSelectDialog.dismiss() }
            })
            imageSelectDialog.show(fragmentManager!!, imageSelectDialog.tag)
        }

        view.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        view.dialog_button_save.setOnClickListener {

            if(selectedFoodType != null || selectedDrinkType != null || selectedDishType != null) {

                val url = when(type){
                    1 -> Routes.menusFoodAddNew
                    2 -> Routes.menusDrinkAddNew
                    3 -> Routes.menusDishAddNew
                    else -> ""
                }

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

                val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("name", view.menu_name.text.toString())
                    .addFormDataPart("description", view.menu_description.text.toString())
                    .addFormDataPart("ingredients", view.menu_ingredients_list.html)
                    .addFormDataPart("price", view.menu_current_price.text.toString())
                    .addFormDataPart("last_price", view.menu_last_price.text.toString())
                    .addFormDataPart("currency", session.branch.restaurant?.config?.currency!!)
                    .addFormDataPart("is_countable", if(view.menu_is_countable.isChecked){ "on" } else { "off" })
                    .addFormDataPart("show_ingredients", if(view.menu_show_ingredients.isChecked) { "on" } else { "off" })
                    .addFormDataPart("quantity", view.menu_quantity.text.toString())
                    .addFormDataPart("for_all_branches", if(view.menu_for_all_branches.isChecked) { "on" } else { "off" })
                    .addFormDataPart("category", selectedCategory.toString())
                    .addFormDataPart("cuisine", selectedCuisine.toString())
                    .addFormDataPart("food_type", (selectedFoodType?:1).toString())
                    .addFormDataPart("drink_type", (selectedDrinkType?:1).toString())
                    .addFormDataPart("dish_time", (selectedDishType?:1).toString())

                menuImages.forEach {
                    try {
                        val image = UtilsFunctions.compressFile(it)
                        val mediaTypePng = "image/png".toMediaType()
                        requestBodyBuilder.addFormDataPart("image", image?.name, RequestBody.create(mediaTypePng, image!!))
                    } catch (e: Exception) {}
                }

                val requestBody = requestBodyBuilder.build()

                val request = Request.Builder()
                    .header("Authorization", session.token)
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

            } else { TingToast(context!!, "Please, Select $typeText Type", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
        }

        return view
    }

    private fun selectCategory(typeText: String, view: View) {
        TingClient.getRequest(Routes.categoriesAll, null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                if (isSuccess) {
                    try {
                        val categories =
                            Gson().fromJson<List<FoodCategory>>(
                                result,
                                object : TypeToken<List<FoodCategory>>() {}.type
                            ).sortedBy { it.id }
                        val selectDialog = SelectDialog()
                        val selectBundle = Bundle()
                        selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select $typeText Category")
                        selectDialog.arguments = selectBundle
                        selectDialog.setItems(categories.map { it.name }, object :
                            SelectItemListener {
                            override fun onSelectItem(position: Int) {
                                val category = categories[position]
                                selectedCategory = category.id
                                view.selected_menu_food_category.text = category.name
                                view.selected_menu_dish_category.text = category.name
                                view.selected_menu_food_category.setTextColor(resources.getColor(R.color.colorGray))
                                view.selected_menu_dish_category.setTextColor(resources.getColor(R.color.colorGray))
                                selectDialog.dismiss()
                            }
                        })
                        selectDialog.show(fragmentManager!!, selectDialog.tag)
                    } catch (e: java.lang.Exception) { }
                } else { TingToast(context!!, result, TingToastType.ERROR).showToast(
                    Toast.LENGTH_LONG) }
            }
        }
    }

    private fun selectCuisine(typeText: String, view: View) {
        val cuisines = session.branch.categories.categories.sortedBy { it.id }
        val selectDialog = SelectDialog()
        val selectBundle = Bundle()
        selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select $typeText Cuisine")
        selectDialog.arguments = selectBundle
        selectDialog.setItems(cuisines.map { it.name }, object : SelectItemListener {
            override fun onSelectItem(position: Int) {
                val cuisine = cuisines[position]
                selectedCuisine = cuisine.id
                view.selected_menu_food_cuisine.text = cuisine.name
                view.selected_menu_dish_cuisine.text = cuisine.name
                view.selected_menu_food_cuisine.setTextColor(resources.getColor(R.color.colorGray))
                view.selected_menu_dish_cuisine.setTextColor(resources.getColor(R.color.colorGray))
                selectDialog.dismiss()
            }
        })
        selectDialog.show(fragmentManager!!, selectDialog.tag)
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog?.window!!.setLayout(width, height)
        }
    }

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }
}