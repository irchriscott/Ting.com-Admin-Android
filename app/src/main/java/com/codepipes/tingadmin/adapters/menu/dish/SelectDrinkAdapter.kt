package com.codepipes.tingadmin.adapters.menu.dish

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.Menu
import com.codepipes.tingadmin.utils.Routes
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_select_assign_waiter.view.*

class SelectDrinkAdapter (private val drinks: List<Menu>, private val selectItemListener: SelectItemListener) : RecyclerView.Adapter<SelectDrinkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SelectDrinkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.row_select_assign_waiter, parent, false)
        return SelectDrinkViewHolder(row)
    }

    override fun getItemCount(): Int = drinks.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SelectDrinkViewHolder, position: Int) {

        if(drinks[position].id != 0) {
            holder.view.waiter_view.visibility = View.VISIBLE
            holder.view.delete_view.visibility = View.GONE
            Picasso.get().load("${Routes.HOST_END_POINT}${drinks[position].images.images[0].image}").into(holder.view.waiter_image)
        } else {
            holder.view.waiter_view.visibility = View.GONE
            holder.view.delete_view.visibility = View.VISIBLE
        }

        holder.view.remove_text.text = "Remove Drink"

        holder.view.waiter_name.text = drinks[position].name
        if(position == drinks.size - 1) { holder.view.select_separator.visibility = View.GONE }
        holder.view.setOnClickListener { selectItemListener.onSelectItem(drinks[position].id) }
    }
}

class SelectDrinkViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}