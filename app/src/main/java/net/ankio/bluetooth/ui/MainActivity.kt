package net.ankio.bluetooth.ui


import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.github.sardine.impl.SardineException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.ankio.bluetooth.R
import net.ankio.bluetooth.databinding.AboutDialogBinding
import net.ankio.bluetooth.databinding.ActivityMainBinding
import net.ankio.bluetooth.databinding.InputLayoutBinding
import net.ankio.bluetooth.utils.HookUtils
import net.ankio.bluetooth.utils.SpUtils
import net.ankio.bluetooth.utils.WebdavUtils
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

        binding.saveWebdav.setOnClickListener {
            if (SpUtils.getBoolean("pref_as_sender", false)) {

            } else {

            }
        }
        binding.search.setOnClickListener {
            start<ScanActivity>()
        }
    }

    fun trySyncFromWebDav() {

    }

    override fun onResume() {
        super.onResume()
        if (HookUtils.getActiveAndSupportFramework()) {
            binding.active.setBackgroundColor(getThemeAttrColor(com.google.android.material.R.attr.colorPrimary))
            binding.imageView.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.ic_success
                )
            )
            binding.msgLabel.text = getString(R.string.active_success)
            binding.imageView.setColorFilter(getThemeAttrColor(com.google.android.material.R.attr.colorOnPrimary))
            binding.msgLabel.setTextColor(getThemeAttrColor(com.google.android.material.R.attr.colorOnPrimary))
        } else {
            binding.active.setBackgroundColor(getThemeAttrColor(com.google.android.material.R.attr.colorErrorContainer))
            binding.imageView.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.ic_error
                )
            )
            binding.msgLabel.text = getString(R.string.active_error)
            binding.imageView.setColorFilter(getThemeAttrColor(com.google.android.material.R.attr.colorOnErrorContainer))
            binding.msgLabel.setTextColor(getThemeAttrColor(com.google.android.material.R.attr.colorOnErrorContainer))
        }


        setMacBluetoothData()

        //如果是发送端，点击保存就启动定时发送服务
        //如果是接收端，在显示同步按钮让用户手动同步/页面切换的时候同步
        if (SpUtils.getBoolean("pref_enable_webdav", false)) {
            if (SpUtils.getBoolean("pref_as_sender", false)) {
                startServer()
            } else {
                getServer()
            }
        }


    }

    fun startServer() {

    }

    private fun getServer() {
        try {
            val bluetoothData = WebdavUtils().getFromServer()
            if (bluetoothData == null) {
                showMsg(R.string.get_bluetooth_error)
                return
            }
            SpUtils.putString("pref_mac", bluetoothData.mac)
            SpUtils.putString("pref_data", bluetoothData.data)
            SpUtils.putString("pref_signal", bluetoothData.rssi)
        } catch (e: SardineException) {
            showMsg(R.string.webdav_error)
            return
        }

    }


    private fun setMacBluetoothData() {
        SpUtils.getString("pref_mac", "").apply {
            binding.macLabel.text = this
        }
        SpUtils.getString("pref_data", "").apply {
            binding.broadcastLabel.text = this
        }
        SpUtils.getString("pref_signal", "").apply {
            binding.signalLabel.text = this
        }
        SpUtils.getBoolean("pref_enable_webdav", false).apply {
            binding.webdavEnable.isSelected = this
            binding.webdavEnable.setOnCheckedChangeListener { _, isChecked ->
                SpUtils.putBoolean("pref_enable_webdav", isChecked)
                if (isChecked) {
                    binding.asSender.visibility = View.GONE
                } else {
                    binding.asSender.visibility = View.VISIBLE
                }
            }
        }
        //是否作为发送端
        SpUtils.getBoolean("pref_as_sender", false).apply {
            binding.asSender.isSelected = this
            binding.asSender.setOnCheckedChangeListener { _, isChecked ->
                SpUtils.putBoolean("pref_as_sender", isChecked)
                if (isChecked) {
                    binding.enable.visibility = View.GONE
                    SpUtils.putBoolean("pref_enable", false)
                } else {
                    binding.enable.visibility = View.VISIBLE
                }
            }

        }
        //是否开启模拟
        SpUtils.getBoolean("pref_enable", false).apply {
            binding.switchButton.isSelected = this
            binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
                SpUtils.putBoolean("pref_enable", isChecked)
            }
        }
        //配置信息
        SpUtils.getString("webdav_server", "https://dav.jianguoyun.com/dav/").apply {
            binding.webdavLabel.text = this
            binding.webdavLabelArea.setOnClickListener {
                showInput(this, getString(R.string.please_input_webdav), object : InputListener {
                    override fun onInput(value: String) {
                        SpUtils.putString("webdav_server", value)
                        binding.webdavLabel.text = value
                    }
                })
            }
        }
        SpUtils.getString("webdav_username", "").apply {
            binding.usernameLabel.text = this
            binding.usernameLabelArea.setOnClickListener {
                showInput(this, getString(R.string.please_input_username), object : InputListener {
                    override fun onInput(value: String) {
                        SpUtils.putString("webdav_username", value)
                        binding.usernameLabel.text = value
                    }
                })
            }
        }
        SpUtils.getString("webdav_password", "").apply {
            binding.passwordLabel.text = this
            binding.passwordLabelArea.setOnClickListener {
                showInput(this, getString(R.string.please_input_webdav), object : InputListener {
                    override fun onInput(value: String) {
                        SpUtils.putString("webdav_password", value)
                        binding.passwordLabel.text = value
                    }
                })
            }
        }
        SpUtils.getString("webdav_last", getString(R.string.webdav_no_sync)).apply {
            binding.lastDate.text = this
        }

    }

    private fun showInput(value: String, title: String, inputListener: InputListener) {

        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetBinding = InputLayoutBinding.inflate(layoutInflater)
        sheetBinding.title.text = title
        sheetBinding.input.setText(value)
        sheetBinding.submit.setOnClickListener {
            inputListener.onInput(sheetBinding.input.text.toString())
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setContentView(sheetBinding.root)

        bottomSheetDialog.show()

    }

    interface InputListener {
        fun onInput(value: String)
    }


}