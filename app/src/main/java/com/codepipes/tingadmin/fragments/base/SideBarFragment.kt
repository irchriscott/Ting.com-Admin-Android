package com.codepipes.tingadmin.fragments.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.activities.base.TingDotCom
import com.codepipes.tingadmin.interfaces.NavigationMenuItemListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_side_bar.view.*
import kotlinx.android.synthetic.main.navigation_header.view.*

class SideBarFragment : Fragment () {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    private val mOnNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener {

        updateItemSelected(it.itemId)

        when(it.itemId) {
            R.id.navigation_dashboard -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_branches -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_categories -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_menus -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_promotions -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_administration -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_restaurant -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tables -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_reservations -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_placements -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_side_bar, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        val header = view.navigation_view.getHeaderView(0)
        header.administrator_name.text = session.name
        header.administrator_email.text = session.email
        Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(header.administrator_image)

        val activeItem = arguments?.getInt("item", 0)
        view.navigation_view.setCheckedItem(activeItem ?: R.id.navigation_dashboard)
        view.navigation_view.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        return view
    }

    private fun updateItemSelected(itemId: Int) {
        val tingActivity = activity as TingDotCom
        tingActivity.navigationView.setCheckedItem(itemId)
    }

    companion object {
        public fun newInstance(itemId: Int) : SideBarFragment {
            return  SideBarFragment().apply {
                val bundle = Bundle()
                bundle.putInt("item", itemId)
                arguments = bundle
            }
        }
     }
}