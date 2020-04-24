package com.codepipes.tingadmin.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.activities.base.TingDotCom
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_side_bar.view.*
import kotlinx.android.synthetic.main.navigation_header.view.*

class SideBarFragment : Fragment () {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    private val mOnNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener {

        when(it.itemId) {
            R.id.navigation_dashboard -> {
                updateItemSelected(it.itemId, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_branches -> {
                updateItemSelected(it.itemId, 1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_categories -> {
                updateItemSelected(it.itemId, 2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_menus -> {
                updateItemSelected(it.itemId, 3)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_promotions -> {
                updateItemSelected(it.itemId, 4)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_administration -> {
                updateItemSelected(it.itemId, 5)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_restaurant -> {
                updateItemSelected(it.itemId, 6)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tables -> {
                updateItemSelected(it.itemId, 7)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_reservations -> {
                updateItemSelected(it.itemId, 8)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_placements -> {
                updateItemSelected(it.itemId, 9)
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

        val navigationMenuView = view.navigation_view.getChildAt(0) as NavigationMenuView
        navigationMenuView.isVerticalScrollBarEnabled = false
        navigationMenuView.isHorizontalScrollBarEnabled = false

        updateNavigationMenu(view)
        showIconsOnlyMenu(view)

        return view
    }

    private fun updateItemSelected(itemId: Int, selectedFragment: Int) {
        val tingActivity = activity as TingDotCom
        tingActivity.navigationView.setCheckedItem(itemId)
        tingActivity.changeMainContainerFragment(selectedFragment, 10)
    }


    private fun updateNavigationMenu(view: View) {
        view.navigation_view.menu.getItem(1).isVisible = session.permissions.contains("can_view_branch")
        view.navigation_view.menu.getItem(2).isVisible = session.permissions.contains("can_view_category")
        view.navigation_view.menu.getItem(3).isVisible = session.permissions.contains("can_view_menu")
        view.navigation_view.menu.getItem(4).isVisible = session.permissions.contains("can_view_promotion")
        view.navigation_view.menu.getItem(5).isVisible =
            session.permissions.contains("can_view_admin") || session.permissions.contains("can_view_all_admin")
        view.navigation_view.menu.getItem(7).isVisible = session.permissions.contains("can_view_table")
        view.navigation_view.menu.getItem(8).isVisible = session.permissions.contains("can_view_booking")
        view.navigation_view.menu.getItem(9).isVisible = session.permissions.contains("can_view_placements")
    }

    private fun showIconsOnlyMenu(view: View) {
        view.navigation_view.getHeaderView(0).visibility = View.GONE
        view.navigation_view.menu.forEach {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.title = ""
        }

        val tingDotCom = activity as TingDotCom
        tingDotCom.mainContainerFrameLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.06f)
        tingDotCom.sideBarFrameLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.94f)
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