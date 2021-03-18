package com.sgpublic.scit.tool.base

//import com.umeng.analytics.MobclickAgent
//import com.umeng.commonsdk.UMConfigure
//import com.umeng.message.IUmengRegisterCallback
//import com.umeng.message.PushAgent
//import org.android.agoo.mezu.MeizuRegister
//import org.android.agoo.xiaomi.MiPushRegistar
import android.app.Application
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle

@Suppress("unused")
class Main : Application() {
    override fun onCreate() {
        super.onCreate()
        MyLog.v("APP启动")
        DialogX.init(this)
        DialogX.globalStyle = MIUIStyle.style()
//        BlurKit.init(this)
//        if (!BuildConfig.DEBUG){
//            initSDK(this)
//        }
    }

//    private fun initSDK(context: Context) {
//        UMConfigure.setLogEnabled(false)
//        UMConfigure.init(
//            context,
//            "5ee64237570df329e3000073",
//            "OFFICIAL",
//            UMConfigure.DEVICE_TYPE_PHONE,
//            "89b9db8aaeaf27142f34264960611e76"
//        )
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO)
//        PushAgent.getInstance(this).register(object : IUmengRegisterCallback {
//            override fun onSuccess(code: String) {}
//
//            override fun onFailure(code: String, code1: String) {}
//        })
//        val manufacturer = Build.MANUFACTURER
//        if (manufacturer != null && manufacturer.isNotEmpty()) {
//            when (manufacturer.toLowerCase(Locale.getDefault())) {
//                "meizu" -> MeizuRegister.register(context, "1008865", "1f796e7094b84d9ca50c0df3e5e85503")
//                "huawei" -> {}
//                "xiaomi" -> MiPushRegistar.register(context, "2882303761518265825", "5751826511825")
//                "oppo" -> {}
//                "vivo" -> {}
//            }
//        }
//    }
}