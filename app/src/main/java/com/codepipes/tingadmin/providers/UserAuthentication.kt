package com.codepipes.tingadmin.providers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.SocketUser
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception

class UserAuthentication(
    private val contxt: Context
){
    private val context: Context = contxt
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()

    private val SESSION_SHARED_PREFERENCES_KEY = "session_admin"

    public fun set(data: String){
        this.sharedPreferencesEditor.putString(SESSION_SHARED_PREFERENCES_KEY, data)
        this.sharedPreferencesEditor.apply()
        this.sharedPreferencesEditor.commit()
    }

    public fun get() : Administrator?{
        return if(this.isLoggedIn()){
            val userString = this.sharedPreferences.getString(SESSION_SHARED_PREFERENCES_KEY, null)
            val gson = Gson()
            gson.fromJson(userString, Administrator::class.java)
        } else { null }
    }

    public fun isLoggedIn(): Boolean{
        return !this.sharedPreferences.getString(SESSION_SHARED_PREFERENCES_KEY, null).isNullOrEmpty()
    }

    public fun socketUser() : SocketUser? {
        return if(this.get() != null) {
            SocketUser(this.get()?.id, 3, this.get()?.name, this.get()?.email, "/tinguploads/${this.get()?.image}", this.get()?.channel)
        } else { null }
    }

    public fun logOut(){
        this.sharedPreferencesEditor.remove(SESSION_SHARED_PREFERENCES_KEY).apply {
            apply()
            commit()
        }
    }

    public fun updateSession() {
        val session = get()
        TingClient.getRequest(Routes.authGetSession, null, session?.token) { _, isSuccess, result ->
            if(isSuccess) {
                try {
                    val admin = Gson().fromJson(result, Administrator::class.java)
                    set(Gson().toJson(admin))
                } catch (e: Exception) {}
            }
        }
    }
}