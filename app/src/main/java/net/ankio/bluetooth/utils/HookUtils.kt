package net.ankio.bluetooth.utils

import net.ankio.bluetooth.BuildConfig

object HookUtils {
    //激活状态
    fun getActiveAndSupportFramework(): Boolean {
        return false
    }
    fun getAppVersion(): Int {
        return BuildConfig.VERSION_CODE
    }
    fun getXposedVersion():Int{
        return 82
    }
}