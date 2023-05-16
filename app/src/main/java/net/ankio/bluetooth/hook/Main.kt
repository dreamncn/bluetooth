package net.ankio.bluetooth.hook

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.ankio.bluetooth.utils.ByteUtils


class Main : IXposedHookLoadPackage {
    private val pref: XSharedPreferences = XSharedPreferences("net.ankio.bluetooth", "config")
    private val tag = "AnkioのBluetooth :"
    fun getString(key: String, value: String?): String {
        reload()
        return pref.getString(key, value) ?: ""
    }



    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        XposedBridge.log("$tag lpparam.packageName")
        if (lpparam == null || !lpparam.packageName.equals("com.android.bluetooth")) return



        XposedBridge.log("$tag 蓝牙模拟启动")
        reload()
        if(!pref.getBoolean("pref_enable",false)){
            XposedBridge.log("$tag 关闭蓝牙模拟功能")
            return
        }
        val cClass = XposedHelpers.findClass("com.android.bluetooth.gatt.GattService", lpparam.classLoader)
        val main = this
        XposedBridge.log("$tag class:$cClass")
        XposedHelpers.findAndHookMethod(cClass, "start", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                var hAdditionalI: Handler?
                val str = "handler"
                hAdditionalI =
                    XposedHelpers.getAdditionalInstanceField(param.thisObject, str) as Handler?
                if (hAdditionalI == null) {
                    hAdditionalI = Handler(Looper.getMainLooper())
                }
                XposedHelpers.setAdditionalInstanceField(param.thisObject, str, hAdditionalI)
                val broadcast = BroadcastBluetooth(param, main, hAdditionalI)
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "runnable", broadcast)
                hAdditionalI.postDelayed(broadcast, 500)
                return
            }
            override fun beforeHookedMethod(param: MethodHookParam) {}
        })
        XposedHelpers.findAndHookMethod(cClass, "stop", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val oAdditionalI = XposedHelpers.getAdditionalInstanceField(param.thisObject, "handler")
                if (oAdditionalI !is Handler) {
                    return
                }
                val oAdditionalI1 = XposedHelpers.getAdditionalInstanceField(param.thisObject, "runnable")
                if (oAdditionalI1 !is Runnable) {
                    return
                }
                oAdditionalI.removeCallbacks(oAdditionalI1)
            }
        })


    }

    private fun reload() {
        if (pref.hasFileChanged()) pref.reload()
    }

    class BroadcastBluetooth(param: XC_MethodHook.MethodHookParam, main: Main, handler: Handler) :
        Any(), Runnable {
        private var __main = main
        private var __param = param
        private var __handler = handler
        @SuppressLint("SuspiciousIndentation")
        override fun run() {
            val mac = __main.getString("pref_mac", "76:A7:8A:67:66:C9")

            val params = arrayOf(
                0x1b, //eventType
                0x00,
                mac,
                0x01, //primaryPhy
                0x00, //secondaryPhy
                0xff, //advertisingSid
                0x7f, //txPower
                __main.getString("pref_rssi", "-50").toInt(), //rssi
                0x00, //periodicAdvInt
                ByteUtils.hexStringToBytes(
                    __main.getString(
                        "pref_data",
                        "02010403033CFE17FF0001B500024271A7B6000000C983926CB1011000000000000000000000000000000000000000000000000000000000000000000000"
                    )
                ),
                mac
            )

            try {
                XposedHelpers.callMethod(__param.thisObject, "onScanResult", *params)
            } catch (e: NoSuchMethodError) {
                try {
                    XposedHelpers.callMethod(__param.thisObject, "onScanResult", *params.copyOf(params.size - 1))
                } catch (e: NoSuchMethodError) {
                    // 处理最终的异常情况
                    XposedBridge.log("${__main.tag} 您的设备不支持，请提取com.android.bluetooth文件提交至github")
                    XposedBridge.log("${__main.tag} 异常：${e.message}" )
                    return
                }
            }


            XposedBridge.log("${__main.tag} mock => $mac")
            __handler.postDelayed(this, 500)
        }
    }


}