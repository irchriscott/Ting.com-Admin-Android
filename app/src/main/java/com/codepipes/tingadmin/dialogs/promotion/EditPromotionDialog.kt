package com.codepipes.tingadmin.dialogs.promotion

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.messages.*
import com.codepipes.tingadmin.dialogs.utils.ImageSelectorDialog
import com.codepipes.tingadmin.dialogs.utils.LinkEditorDialog
import com.codepipes.tingadmin.interfaces.*
import com.codepipes.tingadmin.models.FoodCategory
import com.codepipes.tingadmin.models.MenuPromotion
import com.codepipes.tingadmin.models.RestaurantMenu
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_promotion_edit.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditPromotionDialog : DialogFragment() {

    private var formDialogListener: FormDialogListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    private var selectedPromotionOn: String? = null
    private var selectedSpecificMenu: Int? = null
    private var selectedSpecificCategory: Int? = null
    private val selectedPromotionPeriods = mutableListOf<Int>()
    private var selectedPromotionReductionType: String? = null
    private var selectedSupplementMenu: Int? = null

    private var posterImagePath: String? = null

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_promotion_edit, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()

        val promotion = Gson().fromJson(arguments?.getString(Constants.PROMOTION_KEY), MenuPromotion::class.java)

        view.promotion_occasion.setText(promotion.occasionEvent)
        view.promotion_description.html = promotion.description
        selectedPromotionOn = "0${promotion.promotionItem.type.id}"
        view.selected_promotion_on.text = Constants.PROMOTION_MENU[selectedPromotionOn?:"00"]
        view.selected_promotion_on.setTextColor(context?.resources?.getColor(R.color.colorGray)!!)

        view.promotion_on_label.setTextColor(resources.getColor(R.color.colorLightGray))
        view.selected_promotion_on.setTextColor(resources.getColor(R.color.colorLightGray))
        view.promotion_specific_menu_label.setTextColor(resources.getColor(R.color.colorLightGray))
        view.promotion_specific_category_label.setTextColor(resources.getColor(R.color.colorLightGray))
        view.selected_promotion_specific_category.setTextColor(resources.getColor(R.color.colorLightGray))
        view.selected_promotion_specific_menu.setTextColor(resources.getColor(R.color.colorLightGray))

        view.is_promotion_special.isChecked = promotion.period.isSpecial
        if(promotion.period.isSpecial) {
            view.promotion_start_date.setText(promotion.period.startDate!!.split(" ")[0])
            view.promotion_end_date.setText(promotion.period.endDate!!.split(" ")[0])
        } else {
            selectedPromotionPeriods.addAll(promotion.period.periods)
            view.selected_promotion_special_time.text = selectedPromotionPeriods.map { Constants.PROMOTION_PERIOD[it] }.joinToString(", ")
        }
        view.selected_promotion_special_time.setTextColor(resources.getColor(Constants.SELECT_COLORS[!promotion.period.isSpecial && selectedPromotionPeriods.size > 0]!!))
        view.promotion_special_time_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[!promotion.period.isSpecial]!!))
        view.promotion_start_date_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.period.isSpecial]!!))
        view.promotion_end_date_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.period.isSpecial]!!))
        view.promotion_start_date.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.period.isSpecial]!!))
        view.promotion_end_date.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.period.isSpecial]!!))

        view.promotion_has_reduction.isChecked = promotion.reduction.hasReduction
        selectedPromotionReductionType = promotion.reduction.reductionType
        if(promotion.reduction.hasReduction) {
            view.promotion_reduction_amount.setText(promotion.reduction.amount.toString())
        }
        view.promotion_reduction_amount.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.reduction.hasReduction]!!))
        view.promotion_reduction_amount_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.reduction.hasReduction]!!))
        view.promotion_reduction_type_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.reduction.hasReduction]!!))
        view.selected_promotion_reduction_type.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.reduction.hasReduction && selectedPromotionReductionType != null]!!))

        view.promotion_has_supplement.isChecked = promotion.supplement.hasSupplement
        selectedSupplementMenu = promotion.supplement.supplement?.id
        view.is_supplement_promoted_menu.isChecked = promotion.supplement.hasSupplement && promotion.supplement.isSame
        if(promotion.supplement.hasSupplement) {
            view.promotion_minimum_quantity_supplement.setText(promotion.supplement.minQuantity.toString())
            if(!promotion.supplement.isSame) {
                view.promotion_supplement_quantity.setText(promotion.supplement.quantity.toString())
                view.selected_promotion_supplement_menu.text = promotion.supplement.supplement?.menu?.name
            }
        }
        view.promotion_minimum_quantity_supplement_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.supplement.hasSupplement]!!))
        view.promotion_minimum_quantity_supplement.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.supplement.hasSupplement]!!))
        view.promotion_supplement_quantity.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.supplement.hasSupplement]!!))
        view.is_supplement_promoted_menu_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.supplement.hasSupplement]!!))
        view.promotion_supplement_menu_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.supplement.hasSupplement && !promotion.supplement.isSame]!!))
        view.selected_promotion_supplement_menu.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.supplement.hasSupplement && selectedSupplementMenu != null && !promotion.supplement.isSame]!!))
        view.promotion_supplement_quantity_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.supplement.hasSupplement && !promotion.supplement.isSame]!!))
        view.promotion_supplement_quantity.setTextColor(resources.getColor(Constants.SELECT_COLORS[promotion.supplement.hasSupplement && !promotion.supplement.isSame]!!))

        view.promotion_poster_image.visibility = View.VISIBLE
        Picasso.get().load("${Routes.HOST_END_POINT}${promotion.posterImage}").into(view.promotion_poster_image)

        view.promotion_description.setPlaceholder("Promotion Description")
        view.promotion_description.setEditorHeight(180)
        view.promotion_description.setEditorFontSize(13)
        view.promotion_description.setEditorFontColor(resources.getColor(R.color.colorGray))

        view.action_undo.setOnClickListener(View.OnClickListener { view.promotion_description.undo() })
        view.action_redo.setOnClickListener(View.OnClickListener { view.promotion_description.redo() })
        view.action_bold.setOnClickListener(View.OnClickListener { view.promotion_description.setBold() })
        view.action_italic.setOnClickListener(View.OnClickListener { view.promotion_description.setItalic() })
        view.action_subscript.setOnClickListener(View.OnClickListener { view.promotion_description.setSubscript() })
        view.action_superscript.setOnClickListener(View.OnClickListener { view.promotion_description.setSuperscript() })
        view.action_strikethrough.setOnClickListener(View.OnClickListener { view.promotion_description.setStrikeThrough() })
        view.action_underline.setOnClickListener(View.OnClickListener { view.promotion_description.setUnderline() })
        view.action_indent.setOnClickListener(View.OnClickListener { view.promotion_description.setIndent() })
        view.action_outdent.setOnClickListener(View.OnClickListener { view.promotion_description.setOutdent() })
        view.action_align_left.setOnClickListener(View.OnClickListener { view.promotion_description.setAlignLeft() })
        view.action_align_center.setOnClickListener(View.OnClickListener { view.promotion_description.setAlignCenter() })
        view.action_align_right.setOnClickListener(View.OnClickListener { view.promotion_description.setAlignRight() })
        view.action_insert_bullets.setOnClickListener(View.OnClickListener { view.promotion_description.setBullets() })
        view.action_insert_numbers.setOnClickListener(View.OnClickListener { view.promotion_description.setNumbers() })
        view.action_insert_link.setOnClickListener(View.OnClickListener {
            val linkEditorDialog = LinkEditorDialog()
            linkEditorDialog.setOnLinkSet(object : EditorLinkListener {
                override fun onLinkSet(link: String, title: String) {
                    if(link.isNotBlank() && link.isNotEmpty()){
                        view.promotion_description.insertLink(link, if(title.isNotBlank() && title.isNotEmpty()){ title } else { link })
                        linkEditorDialog.dismiss()
                    } else { TingToast(context!!, "Please, Insert Link URL", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                }
            })
            linkEditorDialog.show(fragmentManager!!, linkEditorDialog.tag)
        })

        view.promotion_on_select.setOnClickListener {
            TingToast(context!!, "Functionality Not Available In Edit", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG)
        }

        view.promotion_specific_menu_select.setOnClickListener {
            TingToast(context!!, "Functionality Not Available In Edit", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG)
        }

        view.promotion_specific_category_select.setOnClickListener {
            TingToast(context!!, "Functionality Not Available In Edit", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG)
        }
        
        view.is_promotion_special.setOnCheckedChangeListener { _, isChecked ->
            view.selected_promotion_special_time.setTextColor(resources.getColor(Constants.SELECT_COLORS[!isChecked && selectedPromotionPeriods.size > 0]!!))
            view.promotion_special_time_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[!isChecked]!!))
            view.promotion_start_date_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.promotion_end_date_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.promotion_start_date.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.promotion_end_date.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
        }

        view.promotion_special_time_select.setOnClickListener {
            if(!view.is_promotion_special.isChecked) {
                val selectDialog = MultipleSelectDialog()
                val selectBundle = Bundle()
                selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Promotion Special Days")
                selectDialog.arguments = selectBundle
                selectDialog.setItems(Constants.PROMOTION_PERIOD.toSortedMap().map { it.value }, selectedPromotionPeriods.map { it - 1 }.sorted(), object :
                    MultipleSelectItemsListener {
                    override fun onSelectItems(items: List<Int>) {
                        selectedPromotionPeriods.addAll(items.map { it + 1 })
                        view.selected_promotion_special_time.text = selectedPromotionPeriods.map { Constants.PROMOTION_PERIOD[it] }.joinToString(", ")
                        view.selected_promotion_special_time.setTextColor(resources.getColor(R.color.colorGray))
                        selectDialog.dismiss()
                    }
                })
                selectDialog.show(fragmentManager!!, selectDialog.tag)
            } else { TingToast(context!!, "Uncheck Is Promotion Special", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG) }
        }

        val calendar = Calendar.getInstance()

        val startDate = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = "yyyy-MM-dd"
            val sdf = SimpleDateFormat(format, Locale.US)
            view.promotion_start_date.setText(sdf.format(calendar.time))
        }

        val endDate = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = "yyyy-MM-dd"
            val sdf = SimpleDateFormat(format, Locale.US)
            view.promotion_end_date.setText(sdf.format(calendar.time))
        }

        view.promotion_start_date.setOnClickListener {
            if(view.is_promotion_special.isChecked) {
                DatePickerDialog(activity!!,
                    R.style.DatePickerAppTheme, startDate, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            } else { TingToast(context!!, "Check Is Promotion Special", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG) }
        }

        view.promotion_end_date.setOnClickListener {
            if(view.is_promotion_special.isChecked) {
                DatePickerDialog(activity!!,
                    R.style.DatePickerAppTheme, endDate, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            } else { TingToast(context!!, "Check Is Promotion Special", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG) }
        }

        view.promotion_has_reduction.setOnCheckedChangeListener { _, isChecked ->
            view.promotion_reduction_amount.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.promotion_reduction_amount_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.promotion_reduction_type_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.selected_promotion_reduction_type.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked && selectedPromotionReductionType != null]!!))
        }

        view.promotion_reduction_type_select.setOnClickListener {
            if(view.promotion_has_reduction.isChecked) {
                val promotionReductionType = hashMapOf<Int, String>(
                    0 to session?.branch?.restaurant!!.config.currency,
                    1 to "%"
                )
                val selectDialog = SelectDialog()
                val selectBundle = Bundle()
                selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Reduction Type")
                selectDialog.arguments = selectBundle
                selectDialog.setItems(promotionReductionType.toSortedMap().map { it.value }, object :
                    SelectItemListener {
                    override fun onSelectItem(position: Int) {
                        selectedPromotionReductionType = promotionReductionType[position]
                        view.selected_promotion_reduction_type.text = selectedPromotionReductionType
                        view.selected_promotion_reduction_type.setTextColor(resources.getColor(R.color.colorGray))
                        selectDialog.dismiss()
                    }
                })
                selectDialog.show(fragmentManager!!, selectDialog.tag)
            } else { TingToast(context!!, "Promotion Has Reduction Is Not Checked", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG) }
        }

        view.promotion_has_supplement.setOnCheckedChangeListener { _, isChecked ->
            view.promotion_minimum_quantity_supplement_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.promotion_minimum_quantity_supplement.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.promotion_supplement_quantity.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.is_supplement_promoted_menu_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked]!!))
            view.promotion_supplement_menu_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked && !view.is_supplement_promoted_menu.isChecked]!!))
            view.selected_promotion_supplement_menu.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked && selectedSupplementMenu != null && !view.is_supplement_promoted_menu.isChecked]!!))
            view.promotion_supplement_quantity_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked && !view.is_supplement_promoted_menu.isChecked]!!))
            view.promotion_supplement_quantity.setTextColor(resources.getColor(Constants.SELECT_COLORS[isChecked && !view.is_supplement_promoted_menu.isChecked]!!))
        }

        view.promotion_supplement_menu_select.setOnClickListener {
            if(!view.is_supplement_promoted_menu.isChecked) {
                TingClient.getRequest(Routes.menusAll, null, session?.token) { _, isSuccess, result ->
                    activity?.runOnUiThread {
                        if (isSuccess) {
                            try {
                                val menus =
                                    Gson().fromJson<List<RestaurantMenu>>(
                                        result,
                                        object : TypeToken<List<RestaurantMenu>>() {}.type
                                    ).sortedBy { it.id }
                                val selectDialog = SelectMenuDialog()
                                val selectBundle = Bundle()
                                selectBundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Promotion Supplement Menu")
                                selectDialog.arguments = selectBundle
                                selectDialog.setMenus(menus, object :
                                    SelectItemListener {
                                    override fun onSelectItem(position: Int) {
                                        val menu = menus[position]
                                        selectedSupplementMenu = menu.id
                                        view.selected_promotion_supplement_menu.text = menu.menu.name
                                        view.selected_promotion_supplement_menu.setTextColor(resources.getColor(R.color.colorGray))
                                        selectDialog.dismiss()
                                    }
                                })
                                selectDialog.show(fragmentManager!!, selectDialog.tag)
                            } catch (e: java.lang.Exception) { }
                        } else { TingToast(context!!, result, TingToastType.ERROR).showToast(
                            Toast.LENGTH_LONG) }
                    }
                }
            } else { TingToast(context!!, "Uncheck Is Supplement The Promoted Menu", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG) }
        }

        view.is_supplement_promoted_menu.setOnCheckedChangeListener { _, isChecked ->
            if(view.promotion_has_supplement.isChecked) {
                view.promotion_supplement_menu_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[!isChecked && view.promotion_has_supplement.isChecked]!!))
                view.selected_promotion_supplement_menu.setTextColor(resources.getColor(Constants.SELECT_COLORS[!isChecked && selectedSupplementMenu != null && view.promotion_has_supplement.isChecked]!!))
                view.promotion_supplement_quantity_label.setTextColor(resources.getColor(Constants.SELECT_COLORS[!isChecked && view.promotion_has_supplement.isChecked]!!))
                view.promotion_supplement_quantity.setTextColor(resources.getColor(Constants.SELECT_COLORS[!isChecked && view.promotion_has_supplement.isChecked]!!))
            } else {
                view.is_supplement_promoted_menu.isChecked = false
                TingToast(context!!, "Promotion Has Supplement Is Not Checked", TingToastType.DEFAULT).showToast(
                Toast.LENGTH_LONG)
            }
        }

        view.promotion_add_poster_image.setOnClickListener {
            val imageSelectDialog = ImageSelectorDialog()
            imageSelectDialog.setMaxImages(1)
            imageSelectDialog.setSelectorType(0)
            imageSelectDialog.setOnImageSelectorListener(object : ImageSelectorListener {
                override fun onMultipleImagesSelected(images: List<String>) {}
                override fun onSingleImageSelected(image: String) {
                    if(image != "") {
                        try {
                            posterImagePath = image
                            val posterImage = File(image)
                            view.promotion_poster_image.visibility = View.VISIBLE
                            Picasso.get().load(posterImage).into(view.promotion_poster_image)
                            imageSelectDialog.dismiss()
                        } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.DEFAULT).showToast(
                            Toast.LENGTH_LONG) }
                    } else { TingToast(context!!, "Image Cannot Be Null", TingToastType.DEFAULT).showToast(
                        Toast.LENGTH_LONG) }
                }
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

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager!!, progressOverlay.tag)

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            val reductionAmount = if(view.promotion_reduction_amount.text.isNotEmpty() && view.promotion_reduction_amount.text.isNotBlank()){
                view.promotion_reduction_amount.text.toString()
            } else { "0" }

            val supplementMinQuantity = if(view.promotion_minimum_quantity_supplement.text.isNotEmpty() && view.promotion_minimum_quantity_supplement.text.isNotBlank()){
                view.promotion_minimum_quantity_supplement.text.toString()
            } else { "0" }

            val supplementQuantity = if(view.promotion_supplement_quantity.text.isNotEmpty() && view.promotion_supplement_quantity.text.isNotBlank()){
                view.promotion_supplement_quantity.text.toString()
            } else { "0" }

            val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("occasion_event", view.promotion_occasion.text.toString())
                .addFormDataPart("description", if(view.promotion_description.html != null) { view.promotion_description.html } else { "" })
                .addFormDataPart("is_special", if(view.is_promotion_special.isChecked) { "on" } else { "off" })
                .addFormDataPart("start_date", view.promotion_start_date.text.toString().replace("\\s", ""))
                .addFormDataPart("end_date", view.promotion_end_date.text.toString().replace("\\s", ""))
                .addFormDataPart("promotion_period", selectedPromotionPeriods.joinToString(","))
                .addFormDataPart("has_reduction", if(view.promotion_has_reduction.isChecked) { "on" } else { "off" })
                .addFormDataPart("amount", reductionAmount)
                .addFormDataPart("reduction_type", selectedPromotionReductionType?:session?.branch?.restaurant?.config!!.currency)
                .addFormDataPart("has_supplement", if(view.promotion_has_supplement.isChecked) { "on" } else { "off" })
                .addFormDataPart("supplement_min_quantity", supplementMinQuantity)
                .addFormDataPart("is_supplement_same", if(view.is_supplement_promoted_menu.isChecked) { "on" } else { "off" })
                .addFormDataPart("supplement", selectedSupplementMenu.toString())
                .addFormDataPart("supplement_quantity", supplementQuantity)
                .addFormDataPart("for_all_branches", "off")

            if(posterImagePath != "") {
                try {
                    val image = UtilsFunctions.compressFile(File(posterImagePath))
                    val mediaTypePng = "image/png".toMediaType()
                    requestBodyBuilder.addFormDataPart("poster_image", image?.name, RequestBody.create(mediaTypePng, image!!))
                } catch (e: Exception) {}
            }

            val requestBody = requestBodyBuilder.build()

            val request = Request.Builder()
                .header("Authorization", session!!.token)
                .url(Routes.promotionUpdate.format(promotion.id))
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