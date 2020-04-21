package com.codepipes.tingadmin.app

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.annotation.Nullable
import androidx.multidex.MultiDex
import com.livefront.bridge.Bridge
import com.livefront.bridge.SavedStateHandler
import icepick.Icepick

class TingDotComApp () : Application() {

    override fun onCreate() {
        super.onCreate()

        Bridge.initialize(this, object : SavedStateHandler {
            override fun saveInstanceState(
                target: Any,
                state: Bundle
            ) {
                Icepick.saveInstanceState(this, state)
            }

            override fun restoreInstanceState(
                target: Any,
                @Nullable state: Bundle?
            ) {
                Icepick.restoreInstanceState(this, state)
            }
        })
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}