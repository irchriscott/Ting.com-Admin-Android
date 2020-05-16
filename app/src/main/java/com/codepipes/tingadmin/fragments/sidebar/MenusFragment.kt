package com.codepipes.tingadmin.fragments.sidebar


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.fragments.sidebar.menus.MenuDishFragment
import com.codepipes.tingadmin.fragments.sidebar.menus.MenuDrinkFragment
import com.codepipes.tingadmin.fragments.sidebar.menus.MenuFoodFragment
import kotlinx.android.synthetic.main.fragment_menus.view.*

class MenusFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_menus, container, false)

        val adapter = MenusViewPagerAdapter(fragmentManager!!)
        adapter.addFragment(MenuFoodFragment(), "FOODS")
        adapter.addFragment(MenuDrinkFragment(), "DRINKS")
        adapter.addFragment(MenuDishFragment(), "DISHES")

        view.menus_viewpager.adapter = adapter
        view.menus_tabs.setupWithViewPager(view.menus_viewpager)

        return view
    }

    internal class MenusViewPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

        private val fragments: MutableList<Fragment> = ArrayList()
        private val fragmentsTitle: MutableList<String> = ArrayList()

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = fragmentsTitle[position]

        public fun addFragment(fragment: Fragment, title: String){
            this.fragments.add(fragment)
            this.fragmentsTitle.add(title)
        }
    }
}
