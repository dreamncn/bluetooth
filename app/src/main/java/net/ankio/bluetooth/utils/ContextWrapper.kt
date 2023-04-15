package net.ankio.bluetooth.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import java.util.Locale

object  ContextWrapper{
    fun wrap(context: Context, newLocale: Locale?): Context {
        val mContext = context
        val res: Resources = mContext.resources
        val configuration: Configuration = res.configuration
        configuration.setLocale(newLocale)
        val localeList = LocaleList(newLocale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        return mContext.createConfigurationContext(configuration)
    }
}