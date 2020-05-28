package com.codepipes.tingadmin.adapters.others

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import kotlinx.android.synthetic.main.row_select.view.*

class MultipleSelectAdapter (private val items: List<String>, private val defaults: List<Int>?) : RecyclerView.Adapter<MultipleSelectViewHolder>() {

    private val selectedItems = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): MultipleSelectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.row_select, parent, false)
        return MultipleSelectViewHolder(row)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MultipleSelectViewHolder, position: Int) {
        holder.view.select_text.text = items[position]
        if(position == items.size - 1) { holder.view.select_separator.visibility = View.GONE }

        if(defaults?.contains(position) == true) {
            selectedItems.add(position)
            holder.view.select_view.setBackgroundColor(holder.view.context.resources.getColor(R.color.colorVeryLightGray))
        }

        holder.view.setOnClickListener {
            if(selectedItems.contains(position)) {
                selectedItems.remove(position)
                holder.view.select_view.setBackgroundColor(holder.view.context.resources.getColor(R.color.colorWhite))
            } else {
                selectedItems.add(position)
                holder.view.select_view.setBackgroundColor(holder.view.context.resources.getColor(R.color.colorVeryLightGray))
            }
        }
    }

    public fun getSelectedItems() : MutableList<Int> = selectedItems
}

class MultipleSelectViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}