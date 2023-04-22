package net.ankio.bluetooth.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.ankio.bluetooth.BuildConfig


class MainServer : IXposedHookLoadPackage {
    private val tag = "AnkioのBluetooth Main:"
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null || !lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) return
        XposedBridge.log("$tag Hook self")
        val cClass = XposedHelpers.findClass("net.ankio.bluetooth.utils.HookUtils", lpparam.classLoader)
        XposedHelpers.findAndHookMethod(
            cClass,
            "getActiveAndSupportFramework",
            XC_MethodReplacement.returnConstant(true)
        )


        XposedHelpers.findAndHookMethod(
            cClass,
            "getXposedVersion"
            ,object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    // 获取应用程序的包信息
                    param?.result = XposedBridge.getXposedVersion()
                }
            }
        )
    }
}
