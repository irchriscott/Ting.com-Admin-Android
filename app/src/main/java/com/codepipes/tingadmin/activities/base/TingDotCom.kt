package com.codepipes.tingadmin.activities.base

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.androidstudy.networkmanager.Tovuti
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.activities.navbar.EditProfile
import com.codepipes.tingadmin.activities.navbar.Privileges
import com.codepipes.tingadmin.activities.navbar.Security
import com.codepipes.tingadmin.custom.Noty
import com.codepipes.tingadmin.fragments.base.DashboardFragment
import com.codepipes.tingadmin.fragments.base.SideBarFragment
import com.codepipes.tingadmin.fragments.sidebar.*
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.providers.PubnubNotification
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.services.PubnubService
import com.codepipes.tingadmin.services.PushNotificationService
import com.codepipes.tingadmin.utils.Routes
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_ting_dot_com.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import java.lang.Exception
import kotlin.system.exitProcess

class TingDotCom : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    public lateinit var navigationView: NavigationView
    private var selectedItem: Int = 0

    public lateinit var sideBarFrameLayout: FrameLayout
    public lateinit var mainContainerFrameLayout: FrameLayout

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
                changeMainContainerFragment(0, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_branches -> {
                changeMainContainerFragment(1, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_categories -> {
                changeMainContainerFragment(2, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_menus -> {
                changeMainContainerFragment(3, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_promotions -> {
                changeMainContainerFragment(4, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_administration -> {
                changeMainContainerFragment(5, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_restaurant -> {
                changeMainContainerFragment(6, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tables -> {
                changeMainContainerFragment(7, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_reservations -> {
                changeMainContainerFragment(8, 0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_placements -> {
                changeMainContainerFragment(9, 0)
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

        PubnubNotification.getInstance(this@TingDotCom, coordinator_layout).initialize()

        userAuthentication = UserAuthentication(this@TingDotCom)
        session = userAuthentication.get()!!

        navigationView = findViewById<NavigationView>(R.id.nav_view) as NavigationView
        sideBarFrameLayout = findViewById<FrameLayout>(R.id.sidebar_container) as FrameLayout
        mainContainerFrameLayout = findViewById<FrameLayout>(R.id.main_container) as FrameLayout

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

        changeMainContainerFragment(selectedItem, 0)
        navigationView.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigationView.setCheckedItem(menuItemIds[selectedItem])

        val navigationMenuView = navigationView.getChildAt(0) as NavigationMenuView
        navigationMenuView.isVerticalScrollBarEnabled = false
        navigationMenuView.isHorizontalScrollBarEnabled = false

        updateNavigationMenu()

        startService(Intent(applicationContext, PushNotificationService::class.java))
        startService(Intent(applicationContext, PubnubService::class.java))
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

    public fun changeMainContainerFragment(selectedFragment: Int, from: Int){
        drawer_layout.closeDrawers()
        if(from == 0) { updateSelectedItem(menuItemIds[selectedFragment]) }
        selectedItem = selectedFragment
        PubnubNotification.getInstance(this@TingDotCom, coordinator_layout).initialize()
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
            R.id.navbar_privileges -> {
                startActivity(Intent(this@TingDotCom, Privileges::class.java))
                return true
            }
            R.id.navbar_security -> {
                startActivity(Intent(this@TingDotCom, Security::class.java))
                return true
            }
            R.id.navbar_history -> {}
            R.id.navbar_notification -> {}
            R.id.navbar_settings -> {}
            R.id.navbar_logout -> {}
        }
        return false
    }

    private fun updateNavigationMenu() {
        navigationView.menu.getItem(1).isVisible = session.permissions.contains("can_view_branch")
        navigationView.menu.getItem(2).isVisible = session.permissions.contains("can_view_category")
        navigationView.menu.getItem(3).isVisible = session.permissions.contains("can_view_menu")
        navigationView.menu.getItem(4).isVisible = session.permissions.contains("can_view_promotion")
        navigationView.menu.getItem(5).isVisible =
            session.permissions.contains("can_view_admin") || session.permissions.contains("can_view_all_admin")
        navigationView.menu.getItem(7).isVisible = session.permissions.contains("can_view_table")
        navigationView.menu.getItem(8).isVisible = session.permissions.contains("can_view_booking")
        navigationView.menu.getItem(9).isVisible = session.permissions.contains("can_view_placements")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(selectedItem::class.java.name, selectedItem)
    }

    override fun onResume() {
        super.onResume()
        updateNavigationMenu()
    }

    override fun onBackPressed() {}

    override fun onDestroy() {
        super.onDestroy()
        Bridge.clear(this)
        Tovuti.from(this).stop()
    }
}
