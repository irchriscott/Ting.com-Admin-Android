package com.codepipes.tingadmin.adapters.menu.dish

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.models.DishFood
import com.codepipes.tingadmin.models.Menu
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_menu_dish_food.view.*
import java.text.NumberFormat

class MenuDishFoodAdapter(private val foods: MutableList<Menu>, private val dishFoods: List<DishFood>) : RecyclerView.Adapter<MenuDishFoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuDishFoodViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.row_menu_dish_food, parent, false)
        return MenuDishFoodViewHolder(row)
    }

    private val checkedFoods = mutableListOf<String>()
    private val foodsQuantities = mutableMapOf<Int, Int>()

    override fun getItemCount(): Int = foods.size

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: MenuDishFoodViewHolder, position: Int) {
        val food = foods[position]

        val index = (0 until food.images.count - 1).random()
        val image = food.images.images[index]
        Picasso.get().load("${Routes.HOST_END_POINT}${image.image}").into(holder.view.menu_image)

        foodsQuantities[position] = 1

        holder.view.menu_name.text = food.name
        holder.view.menu_category_name.text = Constants.FOOD_TYPE[food.foodType]
        holder.view.menu_description.text = food.description
        holder.view.menu_price.text = "${NumberFormat.getNumberInstance().format(food.price)} ${food.currency}".toUpperCase()

        val dishFood = dishFoods.find { it.food == food.id }

        if(dishFood != null) {
            holder.view.menu_dish_food_quantity.setText(dishFood.quantity.toString())
            holder.view.menu_dish_food.isChecked = true
            checkedFoods.add("${food.id}-${position + 1}")
            foodsQuantities[position] = dishFood.quantity
        }

        holder.view.menu_dish_food_quantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!text.isNullOrEmpty() && !text.isNullOrBlank()) {
                    foodsQuantities[position] = text.toString().toInt()
                } else { foodsQuantities[position] = 1 }
            }
        })

        holder.view.menu_dish_food.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                checkedFoods.add("${food.id}-${position + 1}")
            } else { checkedFoods.remove("${food.id}-${position + 1}") }
        }
    }

    public fun getSelectedFoods() : MutableList<String> = checkedFoods

    public fun getQuantities() : MutableMap<Int, Int> = foodsQuantities
}


class MenuDishFoodViewHolder(val view: View) : RecyclerView.ViewHolder(view){}