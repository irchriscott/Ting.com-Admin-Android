package com.codepipes.tingadmin.adapters.reservation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.RestaurantTable
import kotlinx.android.synthetic.main.row_select.view.*

class SelectBookingTableAdapter (
    private val tables: List<RestaurantTable>,
    private val selectItemListener: SelectItemListener
) : RecyclerView.Adapter<SelectBookingTableViewHolder>() {

    private var selectedItemPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SelectBookingTableViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.row_select, parent, false)
        return SelectBookingTableViewHolder(row)
    }

    override fun getItemCount(): Int = tables.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SelectBookingTableViewHolder, position: Int) {
        val table = tables[position]
        holder.view.select_text.text = "${table.number} (${table.maxPeople} People)"
        if(position == selectedItemPosition) { holder.view.setBackgroundColor(holder.view.context.resources.getColor(R.color.colorVeryLightGray)) }
        if(position == tables.size - 1) { holder.view.select_separator.visibility = View.GONE }
        holder.view.setOnClickListener {
            selectItemListener.onSelectItem(table.id)
            val previousSelectedItem = selectedItemPosition
            selectedItemPosition = position
            notifyItemChanged(previousSelectedItem)
            notifyItemChanged(position)
        }
    }
}

class SelectBookingTableViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}