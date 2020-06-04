package com.codepipes.tingadmin.activities.navbar

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.PubnubNotification
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_security.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Security : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    @SuppressLint("PrivateResource", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        setSupportActionBar(toolbar)

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0F
        supportActionBar?.title = "Security".toUpperCase()

        try {
            val upArrow = ContextCompat.getDrawable(this@Security,
                R.drawable.abc_ic_ab_back_material
            )
            upArrow!!.setColorFilter(
                ContextCompat.getColor(this@Security,
                    R.color.colorPrimary
                ), PorterDuff.Mode.SRC_ATOP)
            supportActionBar!!.setHomeAsUpIndicator(upArrow)
        } catch (e: java.lang.Exception) {}

        PubnubNotification.getInstance(this@Security, main_container).initialize()

        userAuthentication = UserAuthentication(this@Security)
        session = userAuthentication.get()!!

        admin_name.text = session.name
        admin_username.text = session.username.toLowerCase()
        admin_email.text = session.email
        Picasso.get().load("${Routes.HOST_END_POINT}${session.image}").into(admin_image)

        submit_password_update.setOnClickListener {
            val data = HashMap<String, String>()
            data["oldpass"] = current_password.text.toString()
            data["newpass"] = new_password.text.toString()
            data["confirmpass"] = confirm_password.text.toString()

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(supportFragmentManager, progressOverlay.tag)

            TingClient.postRequest(Routes.updateAdminPassword, data, null, session.token) { _, isSuccess, result ->
                runOnUiThread {
                    progressOverlay.dismiss()
                    if(isSuccess) {
                        val serverResponse = Gson().fromJson(result, ServerResponse::class.java)
                        TingToast(this@Security, serverResponse.message,
                            if(serverResponse.type == "success") { TingToastType.SUCCESS }
                            else { TingToastType.ERROR }).showToast(Toast.LENGTH_LONG)
                    } else { TingToast(this@Security, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
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
    }
}
