package net.ankio.bluetooth.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainServer : IXposedHookLoadPackage {
    private val tag = "AnkioのBluetooth Main:"
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null || !lpparam.packageName.equals("net.ankio.bluetooth")) return
        XposedBridge.log("$tag Hook App自己")
        val cClass =
            XposedHelpers.findClass("net.ankio.bluetooth.utils.HookUtils", lpparam.classLoader)
        val main = this
        XposedHelpers.findAndHookMethod(
            cClass,
            "getActiveAndSupportFramework",
            XC_MethodReplacement.returnConstant(true)
        )
    }
}
