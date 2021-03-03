package com.sgpublic.cgk.tool.base

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import com.sgpublic.cgk.tool.BuildConfig
import com.umeng.analytics.MobclickAgent
import com.umeng.analytics.MobclickAgent.EScenarioType
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import org.android.agoo.mezu.MeizuRegister
import org.android.agoo.xiaomi.MiPushRegistar
import java.util.*


class Main : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG){
            initSDK(this)
        } else {
            MyLog.setup()
        }
    }

    private fun initSDK(context: Context) {
        UMConfigure.setLogEnabled(false)
        UMConfigure.init(
            context,
            "5ee64237570df329e3000073",
            "OFFICIAL",
            UMConfigure.DEVICE_TYPE_PHONE,
            "89b9db8aaeaf27142f34264960611e76"
        )
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO)
        PushAgent.getInstance(this).register(object : IUmengRegisterCallback {
            override fun onSuccess(code: String) {}

            override fun onFailure(code: String, code1: String) {}
        })
        val manufacturer = Build.MANUFACTURER
        if (manufacturer != null && manufacturer.isNotEmpty()) {
            when (manufacturer.toLowerCase(Locale.getDefault())) {
                "meizu" -> MeizuRegister.register(context, "1008865", "1f796e7094b84d9ca50c0df3e5e85503")
                "huawei" -> {}
                "xiaomi" -> MiPushRegistar.register(context, "2882303761518265825", "5751826511825")
                "oppo" -> {}
                "vivo" -> {}
            }
        }
    }
}