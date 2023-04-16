package net.ankio.bluetooth.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.HtmlCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.ankio.bluetooth.BuildConfig
import net.ankio.bluetooth.R
import net.ankio.bluetooth.databinding.AboutDialogBinding
import net.ankio.bluetooth.databinding.ActivityMainBinding
import net.ankio.bluetooth.utils.HookUtils
import net.ankio.bluetooth.utils.SpUtils
import rikka.html.text.toHtml

class MainActivity : BaseActivity() {
    //视图绑定
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbarLayout = binding.toolbarLayout
        toolbar = binding.toolbar
        scrollView = binding.scrollView

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.theme -> {
                  //  SettingsFragment().show(supportFragmentManager, "Settings")
                    start<SettingsActivity>()
                    true
                }
                R.id.more -> {

                    val binding = AboutDialogBinding.inflate(LayoutInflater.from(this), null, false)
                    binding.sourceCode.movementMethod = LinkMovementMethod.getInstance()
                    binding.sourceCode.text = getString(
                        R.string.about_view_source_code,
                        "<b><a href=\"https://github.com/RikkaApps/Shizuku\">GitHub</a></b>"
                    ).toHtml()

                    binding.versionName.text = packageManager.getPackageInfo(packageName, 0).versionName
                    MaterialAlertDialogBuilder(this)
                        .setView(binding.root)
                        .show()


                    true
                }
                else -> false
            }

        }

        onViewCreated()
    }

    override fun onResume() {
        super.onResume()
        if(HookUtils.getActiveAndSupportFramework()){
            binding.active.setBackgroundColor(getThemeAttrColor(com.google.android.material.R.attr.colorPrimary))
            binding.imageView.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_success))
            binding.msgLabel.text = getString(R.string.active_success)
            binding.imageView.setColorFilter(getThemeAttrColor(com.google.android.material.R.attr.colorOnPrimary))
            binding.msgLabel.setTextColor(getThemeAttrColor(com.google.android.material.R.attr.colorOnPrimary))
        }else{
            binding.active.setBackgroundColor(getThemeAttrColor(com.google.android.material.R.attr.colorErrorContainer))
            binding.imageView.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_error))
            binding.msgLabel.text = getString(R.string.active_error)
            binding.imageView.setColorFilter(getThemeAttrColor(com.google.android.material.R.attr.colorOnErrorContainer))
            binding.msgLabel.setTextColor(getThemeAttrColor(com.google.android.material.R.attr.colorOnErrorContainer))
        }


        setMacBluetoothData()


    }

    private fun setMacBluetoothData(){
        SpUtils.getString("pref_mac","").apply {
            binding.macLabel.text = this
        }
        SpUtils.getString("pref_data","").apply {
            binding.broadcastLabel.text = this
        }
        SpUtils.getString("pref_signal","").apply {
            binding.signalLabel.text = this
        }
    }


}