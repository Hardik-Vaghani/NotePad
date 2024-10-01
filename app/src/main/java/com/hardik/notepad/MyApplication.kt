package com.hardik.notepad

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.hardik.notepad.common.Constants.BASE_TAG
import dagger.hilt.android.HiltAndroidApp
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmMigration
import io.realm.RealmSchema

@HiltAndroidApp
class MyApplication : Application() {
    private val TAG = BASE_TAG + MyApplication::class.java.simpleName

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        val id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val name = Settings.Global.getString(contentResolver, "device_name") ?: ""
        val deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        Log.e(TAG, "onCreate: $id \n $name \n $deviceModel")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "onTerminate: ")
    }

}


