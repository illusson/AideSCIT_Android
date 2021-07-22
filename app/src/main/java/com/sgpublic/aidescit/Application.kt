package com.sgpublic.aidescit

import android.app.Application
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle
import com.sgpublic.aidescit.manager.ConfigManager
import com.sgpublic.aidescit.util.CrashHandler
import com.sgpublic.aidescit.util.MyLog

@Suppress("unused")
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        MyLog.v("APP启动")
        CrashHandler.init(this)
        ConfigManager.init(this)
        XXPermissions.setScopedStorage(true)
        DialogX.init(this)
        DialogX.globalStyle = MIUIStyle.style()
    }
}