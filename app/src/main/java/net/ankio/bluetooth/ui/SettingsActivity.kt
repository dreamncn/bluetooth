package net.ankio.bluetooth.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.text.HtmlCompat
import com.quickersilver.themeengine.ThemeChooserDialogBuilder
import com.quickersilver.themeengine.ThemeEngine
import com.quickersilver.themeengine.ThemeMode
import net.ankio.bluetooth.App
import net.ankio.bluetooth.R
import net.ankio.bluetooth.databinding.SettingsActivityBinding
import net.ankio.bluetooth.utils.CustomTabsHelper
import net.ankio.bluetooth.utils.LocaleDelegate
import net.ankio.bluetooth.utils.SpUtils
import net.ankio.utils.LangList
import java.util.Locale


class SettingsActivity : BaseActivity() {
    private lateinit var binding: SettingsActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbarLayout = binding.toolbarLayout
        toolbar = binding.toolbar
        scrollView = binding.scrollView
        initView()
        onViewCreated()
    }

     private fun initView(){
         //toolbar 设置返回
        toolbar.setNavigationOnClickListener { finish(); }
         //匿名分析
         SpUtils.getBoolean("app_center_analyze",false).apply { binding.analyze.isChecked = this }
         binding.AnalyzeView.setOnClickListener {
             binding.analyze.isChecked = !binding.analyze.isChecked
             SpUtils.putBoolean("app_center_analyze",binding.analyze.isChecked )
         }
         binding.analyze.setOnCheckedChangeListener { _, isChecked ->  SpUtils.putBoolean("app_center_analyze",isChecked ) }
         //语言配置
         val languages: ArrayList<String> = ArrayList()

         LangList.LOCALES.forEach {
             if (it.equals("SYSTEM")) {
                 languages.add(getString(R.string.lang_follow_system))
             }else{
                 val locale = Locale.forLanguageTag(it)
                 languages.add(
                     HtmlCompat.fromHtml(locale.getDisplayName(locale), HtmlCompat.FROM_HTML_MODE_LEGACY)
                         .toString())
             }
         }

         SpUtils.getString("setting_language","SYSTEM").apply { binding.settingLangDesc.text = if (this == "SYSTEM") getString(R.string.lang_follow_system) else Locale.forLanguageTag(this).displayName }



         val listPopupWindow = ListPopupWindow(this, null)

         listPopupWindow.anchorView =  binding.settingLangDesc

         val adapter = ArrayAdapter(this, R.layout.list_popup_window_item,  languages)
         listPopupWindow.setAdapter(adapter)
         listPopupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT

         listPopupWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
             val value = LangList.LOCALES[position]
             binding.settingLangDesc.text = languages[position]
             SpUtils.putString("setting_language",value)
             val locale = App.getLocale(value)
             if(locale===null)return@setOnItemClickListener
             listPopupWindow.dismiss()
             LocaleDelegate.defaultLocale = App.getLocale()
             recreateInit()
         }

         binding.settingLang.setOnClickListener{ listPopupWindow.show() }
         //翻译
         binding.settingTranslate.setOnClickListener{
             CustomTabsHelper.launchUrlOrCopy(this, getString(R.string.translation_url))
         }

         //主题
         val color = ThemeEngine.getInstance(this).staticTheme.primaryColor

         binding.colorSwitch.setCardBackgroundColor(getColor(color))

         binding.settingTheme.setOnClickListener {
             ThemeChooserDialogBuilder(this)
                 .setTitle(R.string.choose_theme)
                 .setPositiveButton(getString(R.string.ok)) { _, theme ->
                     ThemeEngine.getInstance(this).staticTheme = theme
                     recreateInit()
                 }
                 .setNegativeButton(getString(R.string.close))
                 .setNeutralButton(getString(R.string.default_theme)) { _, _ ->
                     ThemeEngine.getInstance(this).resetTheme()
                     recreateInit()
                 }
                 .setIcon(R.drawable.ic_theme)
                 .create()
                 .show()
         }

         val stringList = arrayListOf(getString(R.string.always_off),getString(R.string.always_on),getString(R.string.lang_follow_system))

         binding.settingDarkTheme.text = when(ThemeEngine.getInstance(this@SettingsActivity).themeMode){
             ThemeMode.DARK -> stringList[1]
             ThemeMode.LIGHT -> stringList[0]
             else  -> stringList[2]
         }

         val listPopupThemeWindow = ListPopupWindow(this, null)

         listPopupThemeWindow.anchorView =  binding.settingDarkTheme

         listPopupThemeWindow.setAdapter(ArrayAdapter(this, R.layout.list_popup_window_item,  stringList))
         listPopupThemeWindow.width = WindowManager.LayoutParams.WRAP_CONTENT

         listPopupThemeWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
             binding.settingDarkTheme.text = stringList[position]
             ThemeEngine.getInstance(this).themeMode = when(position){
                 1 -> ThemeMode.DARK
                 0 -> ThemeMode.LIGHT
                 else -> ThemeMode.AUTO
             }
             listPopupThemeWindow.dismiss()
         }

         binding.settingDark.setOnClickListener{ listPopupThemeWindow.show() }


         binding.alwaysDark.isChecked = ThemeEngine.getInstance(this).isTrueBlack
         binding.settingUseDarkTheme.setOnClickListener {
             ThemeEngine.getInstance(this).isTrueBlack = !binding.alwaysDark.isChecked
             binding.alwaysDark.isChecked = !binding.alwaysDark.isChecked
         }
         binding.alwaysDark.setOnCheckedChangeListener { _, isChecked ->  ThemeEngine.getInstance(this).isTrueBlack =  isChecked //;recreateInit()
         }
         if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S){
             binding.settingUseSystemTheme.visibility = View.GONE
         }

         binding.systemColor.isChecked = ThemeEngine.getInstance(this).isDynamicTheme
         binding.settingUseSystemTheme.setOnClickListener {
             ThemeEngine.getInstance(this).isDynamicTheme = !binding.alwaysDark.isChecked
             binding.alwaysDark.isChecked = !binding.alwaysDark.isChecked
         }
         binding.systemColor.setOnCheckedChangeListener { _, isChecked ->
             ThemeEngine.getInstance(this).isDynamicTheme = isChecked
         }

     }





}