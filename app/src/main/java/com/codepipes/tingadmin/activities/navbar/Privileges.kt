package com.codepipes.tingadmin.activities.navbar

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Permission
import com.codepipes.tingadmin.providers.LocalData
import com.codepipes.tingadmin.providers.PubnubNotification
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_privileges.*
import kotlinx.android.synthetic.main.row_checkbox.view.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Privileges : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    private lateinit var localData: LocalData

    private var handler: Handler? = null
    private lateinit var pubnubNotification: PubnubNotification

    private val runnable = Runnable {
        val permissions = localData.getPermissions()
        if(permissions.isNotEmpty()) { setupPermissions(permissions) }
    }

    @SuppressLint("DefaultLocale", "PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privileges)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        setSupportActionBar(toolbar)

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0F
        supportActionBar?.title = "Permissions & Privileges".toUpperCase()

        try {
            val upArrow = ContextCompat.getDrawable(this@Privileges,
                R.drawable.abc_ic_ab_back_material
            )
            upArrow!!.setColorFilter(
                ContextCompat.getColor(this@Privileges,
                    R.color.colorPrimary
                ), PorterDuff.Mode.SRC_ATOP)
            supportActionBar!!.setHomeAsUpIndicator(upArrow)
        } catch (e: java.lang.Exception) {}

        pubnubNotification = PubnubNotification.getInstance(this@Privileges, main_container, supportFragmentManager)
        pubnubNotification.initialize()

        localData = LocalData(this@Privileges)

        userAuthentication = UserAuthentication(this@Privileges)
        session = userAuthentication.get()!!

        admin_name.text = session.name
        admin_username.text = session.username.toLowerCase()
        admin_email.text = session.email
        Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(admin_image)

        handler = Handler()
        handler?.postDelayed(runnable, 2000)
        val permissions = localData.getPermissions()

        TingClient.getRequest(Routes.permissionsAll, null, null) { _, isSuccess, result ->
            if(isSuccess) {
                runOnUiThread {
                    val perms = Gson().fromJson<MutableList<Permission>>(result, object : TypeToken<MutableList<Permission>>(){}.type)
                    localData.savePermissions(Gson().toJson(perms))
                    if(permissions.isEmpty()) { handler?.postDelayed(runnable, 2000) }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun setupPermissions(permissions: MutableList<Permission>) {

        perms_restaurants.removeAllViews()
        perms_administrators.removeAllViews()
        perms_tables.removeAllViews()
        perms_promotions.removeAllViews()
        perms_bills.removeAllViews()
        perms_booking.removeAllViews()
        perms_categories.removeAllViews()
        perms_branches.removeAllViews()
        perms_orders.removeAllViews()
        perms_menus.removeAllViews()
        perms_management.removeAllViews()
        perms_placement.removeAllViews()

        val restaurantPerms = permissions.filter { it.category == "restaurant" }
        val administratorPerms = permissions.filter { it.category == "admin" }
        val tablePerms = permissions.filter { it.category == "table" }
        val promotionPerms = permissions.filter { it.category == "promotion" }
        val billPerms = permissions.filter { it.category == "bill" }
        val bookingPerms = permissions.filter { it.category == "booking" }

        val categoryPerms = permissions.filter { it.category == "category" }
        val branchPerms = permissions.filter { it.category == "branch" }
        val orderPerms = permissions.filter { it.category == "orders" }
        val menusPerms = permissions.filter { it.category == "menu" }
        val managementPerms = permissions.filter { it.category == "management" }
        val placementPerms = permissions.filter { it.category == "placement" }

        if(session.branch.type != 1) {
            perms_tables_view.visibility = View.GONE
            perms_bills_view.visibility = View.GONE
            perms_booking_view.visibility = View.GONE
            perms_orders_view.visibility = View.GONE
            perms_management_view.visibility = View.GONE
            perms_placement_view.visibility = View.GONE
        }

        restaurantPerms.forEach { perms_restaurants.addView(permissionView(it)) }
        administratorPerms.forEach { perms_administrators.addView(permissionView(it)) }
        tablePerms.forEach { perms_tables.addView(permissionView(it)) }
        promotionPerms.forEach { perms_promotions.addView(permissionView(it)) }
        billPerms.forEach { perms_bills.addView(permissionView(it)) }
        bookingPerms.forEach { perms_booking.addView(permissionView(it)) }

        categoryPerms.forEach { perms_categories.addView(permissionView(it)) }
        branchPerms.forEach { perms_branches.addView(permissionView(it)) }
        orderPerms.forEach { perms_orders.addView(permissionView(it)) }
        menusPerms.forEach { perms_menus.addView(permissionView(it)) }
        managementPerms.forEach { perms_management.addView(permissionView(it)) }
        placementPerms.forEach { perms_placement.addView(permissionView(it)) }
    }

    @SuppressLint("InflateParams")
    private fun permissionView(permission: Permission): View {
        val view = LayoutInflater.from(this@Privileges).inflate(R.layout.row_checkbox, null, false)
        view.filter_checkbox.isChecked = session.permissions.contains(permission.permission)
        view.filter_name.text = permission.title
        view.filter_checkbox.isClickable = false
        return view
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        pubnubNotification.close()
        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Bridge.saveInstanceState(this, outState)
        outState.clear()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        Bridge.saveInstanceState(this, outState)
        outState.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { outPersistentState?.clear() }
    }

    override fun onDestroy() {
        super.onDestroy()
        Bridge.clear(this)
        handler?.removeCallbacks(runnable)
        pubnubNotification.close()
    }
}
