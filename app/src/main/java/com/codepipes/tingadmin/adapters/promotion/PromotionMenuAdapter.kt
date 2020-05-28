package com.codepipes.tingadmin.adapters.promotion

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.RestaurantMenu
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_select_assign_waiter.view.*

class PromotionMenuAdapter (private val menus: List<RestaurantMenu>, private val selectItemListener: SelectItemListener) : RecyclerView.Adapter<PromotionMenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): PromotionMenuViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.row_select_assign_waiter, parent, false)
        return PromotionMenuViewHolder(row)
    }

    override fun getItemCount(): Int = menus.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PromotionMenuViewHolder, position: Int) {

        val menu = menus[position]

        holder.view.waiter_name.text = menu.menu.name
        Picasso.get().load("${Routes.HOST_END_POINT}${menu.menu.images.images[0].image}").into(holder.view.waiter_image)
        if(position == menus.size - 1) { holder.view.select_separator.visibility = View.GONE }
        holder.view.setOnClickListener { selectItemListener.onSelectItem(menu.id) }
        holder.view.waiter_extra.visibility = View.VISIBLE
        when(menu.type.id) {
            1 -> holder.view.waiter_extra.text = "${menu.type.name}, ${Constants.FOOD_TYPE[menu.menu.foodType]}"
            2 -> holder.view.waiter_extra.text = "${menu.type.name}, ${Constants.DRINK_TYPE[menu.menu.drinkType]}"
            3 -> holder.view.waiter_extra.text = "${menu.type.name}, ${Constants.DISH_TIME[menu.menu.dishTime]}"
        }
    }
}

class PromotionMenuViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}