package com.codepipes.tingadmin.adapters.table

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Waiter
import com.codepipes.tingadmin.utils.Routes
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_select_assign_waiter.view.*

class SelectWaiterTableAdapter (private val waiters: List<Waiter>, private val selectItemListener: SelectItemListener) : RecyclerView.Adapter<SelectWaiterTableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SelectWaiterTableViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.row_select_assign_waiter, parent, false)
        return SelectWaiterTableViewHolder(row)
    }

    override fun getItemCount(): Int = waiters.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SelectWaiterTableViewHolder, position: Int) {

        if(waiters[position].id != 0) {
            holder.view.waiter_view.visibility = View.VISIBLE
            holder.view.delete_view.visibility = View.GONE
        } else {
            holder.view.waiter_view.visibility = View.GONE
            holder.view.delete_view.visibility = View.VISIBLE
        }

        holder.view.waiter_name.text = waiters[position].name
        Picasso.get().load("${Routes.HOST_END_POINT}${waiters[position].image}").into(holder.view.waiter_image)
        if(position == waiters.size - 1) { holder.view.select_separator.visibility = View.GONE }
        holder.view.setOnClickListener { selectItemListener.onSelectItem(waiters[position].id) }
    }
}

class SelectWaiterTableViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}