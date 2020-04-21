package com.codepipes.tingadmin.activities.base

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.activities.navbar.EditProfile
import com.codepipes.tingadmin.fragments.base.DashboardFragment
import com.codepipes.tingadmin.fragments.base.SideBarFragment
import com.codepipes.tingadmin.fragments.sidebar.*
import com.codepipes.tingadmin.interfaces.NavigationMenuItemListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.android.gms.location.places.Place
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_ting_dot_com.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import java.lang.Exception

class TingDotCom : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    public lateinit var navigationView: NavigationView
    private var selectedItem: Int = 0

    private val menuFragments = arrayListOf<Fragment>(
        DashboardFragment(),
        BranchesFragment(),
        CategoriesFragment(),
        MenusFragment(),
        PromotionsFragment(),
        AdministratorsFragment(),
        RestaurantFragment(),
        TablesFragment(),
        ReservationsFragment(),
        PlacementsFragment()
    )

    private val menuItemIds = arrayListOf<Int>(
        R.id.navigation_dashboard,
        R.id.navigation_branches,
        R.id.navigation_categories,
        R.id.navigation_menus,
        R.id.navigation_promotions,
        R.id.navigation_administration,
        R.id.navigation_restaurant,
        R.id.navigation_tables,
        R.id.navigation_reservations,
        R.id.navigation_placements
    )

    private val mOnNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener {

        when(it.itemId) {
            R.id.navigation_dashboard -> {
                changeMainContainerFragment(0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_branches -> {
                changeMainContainerFragment(1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_categories -> {
                changeMainContainerFragment(2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_menus -> {
                changeMainContainerFragment(3)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_promotions -> {
                changeMainContainerFragment(4)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_administration -> {
                changeMainContainerFragment(5)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_restaurant -> {
                changeMainContainerFragment(6)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tables -> {
                changeMainContainerFragment(7)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_reservations -> {
                changeMainContainerFragment(8)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_placements -> {
                changeMainContainerFragment(9)
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt(selectedItem::class.java.name)
        }

        setContentView(R.layout.activity_ting_dot_com)

        setSupportActionBar(toolbar)

        userAuthentication = UserAuthentication(this@TingDotCom)
        session = userAuthentication.get()!!

        navigationView = findViewById<NavigationView>(R.id.nav_view) as NavigationView

        val actionBarDrawerToggle = ActionBarDrawerToggle(this@TingDotCom, drawer_layout,
            R.string.global_drawer_open,
            R.string.global_drawer_close
        )
        drawer_layout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        restaurant_name.text = "${session.branch.restaurant?.name}, ${session.branch.name}"
        Picasso.get().load(session.branch.restaurant?.logoURL()).into(restaurant_logo)

        val header = nav_view.getHeaderView(0)
        header.administrator_name.text = session.name
        header.administrator_email.text = session.email
        Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(header.administrator_image)

        restaurant_logo.setOnClickListener {
            try { drawer_layout.openDrawer(GravityCompat.START) } catch (e: Exception) {}
        }

        updateSelectedItem(R.id.navigation_dashboard)

        changeMainContainerFragment(selectedItem)
        navigationView.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigationView.setCheckedItem(menuItemIds[selectedItem])
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navbar_menu, menu)
        if (menu is MenuBuilder) {
            val m = menu as MenuBuilder
            m.setOptionalIconsVisible(true)
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateSelectedItem(itemId: Int) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val sideBarFragment = SideBarFragment.newInstance(itemId)
        fragmentTransaction.replace(R.id.sidebar_container, sideBarFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun changeMainContainerFragment(selectedFragment: Int){
        drawer_layout.closeDrawers()
        updateSelectedItem(menuItemIds[selectedFragment])
        selectedItem = selectedFragment
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_container, menuFragments[selectedFragment])
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navbar_edit_profile -> {
                startActivity(Intent(this@TingDotCom, EditProfile::class.java))
                return true
            }
            R.id.navbar_privileges -> {}
            R.id.navbar_security -> {}
            R.id.navbar_history -> {}
            R.id.navbar_notification -> {}
            R.id.navbar_settings -> {}
            R.id.navbar_logout -> {}
        }
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(selectedItem::class.java.name, selectedItem)
    }
}
