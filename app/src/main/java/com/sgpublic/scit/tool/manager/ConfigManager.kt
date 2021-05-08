package com.sgpublic.scit.tool.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.kongzue.dialogx.impl.ActivityLifecycleImpl
import java.lang.ref.WeakReference

object ConfigManager {
    private lateinit var contextWeakReference: WeakReference<Activity>

    fun init(context: Application){
        ActivityLifecycleImpl.init(context) {
            contextWeakReference = WeakReference(it)
        }
    }

    fun getString(key: String, defValue: String = "") = sharedPreferences.getString(key, defValue).toString()
    fun getInt(key: String, defValue: Int = 0) = sharedPreferences.getInt(key, defValue)
    fun getLong(key: String, defValue: Long = 0L) = sharedPreferences.getLong(key, defValue)
    fun getBoolean(key: String, defValue: Boolean = false) = sharedPreferences.getBoolean(key, defValue)

    fun putString(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }
    fun putInt(key: String, value: Int) {
        sharedPreferences.edit()
            .putInt(key, value)
            .apply()
    }
    fun putLong(key: String, value: Long) {
        sharedPreferences.edit()
            .putLong(key, value)
            .apply()
    }
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    private val sharedPreferences: SharedPreferences
        get() = contextWeakReference.get()!!.getSharedPreferences("user", Context.MODE_PRIVATE)
}