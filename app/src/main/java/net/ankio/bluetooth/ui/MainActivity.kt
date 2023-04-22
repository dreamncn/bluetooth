package net.ankio.bluetooth.ui


import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ankio.bluetooth.BuildConfig
import net.ankio.bluetooth.R
import net.ankio.bluetooth.service.SendWebdavServer
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
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "MainActivity"
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbarLayout = binding.toolbarLayout
        toolbar = binding.toolbar
        scrollView = binding.scrollView

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.theme -> {
                    Log.i(tag,"setting menu click!")
                    start<SettingsActivity>()
                    true
                }
                R.id.more -> {
                    Log.i(tag,"about menu click!")
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

        binding.startWebdav.setOnClickListener {
            serverConnect()
        }
        binding.search.setOnClickListener {

            start<ScanActivity>()
        }

    }

    private fun serverConnect(){
        if (SpUtils.getBoolean("pref_enable_webdav", false)) {

            if (SpUtils.getBoolean("pref_as_sender", false)) {

                startServer()
            } else {

                syncFromServer()
            }
            refreshStatus()
        }
    }



    private fun setActive(@StringRes text: Int, @AttrRes backgroundColor:Int, @AttrRes textColor:Int, @DrawableRes drawable:Int){
        binding.active.setBackgroundColor(getThemeAttrColor(backgroundColor))
        binding.imageView.setImageDrawable(
            AppCompatResources.getDrawable(
                this,
                drawable
            )
        )
        binding.msgLabel.text = getString(text)
        binding.imageView.setColorFilter(getThemeAttrColor(textColor))
        binding.msgLabel.setTextColor(getThemeAttrColor(textColor))
    }
    private fun refreshStatus(){
        if (SpUtils.getBoolean("pref_enable_webdav", false)) {
            if (SpUtils.getBoolean("pref_as_sender", false)) {
                if(!SendWebdavServer.isRunning){
                    setActive(R.string.server_error,com.google.android.material.R.attr.colorErrorContainer,com.google.android.material.R.attr.colorOnErrorContainer, R.drawable.ic_error)
                }else{
                    setActive(R.string.server_working,com.google.android.material.R.attr.colorPrimary,com.google.android.material.R.attr.colorOnPrimary,R.drawable.ic_success)
                }
                return
            }
        }
        if (HookUtils.getActiveAndSupportFramework()) {
            if(HookUtils.getAppVersion()!=BuildConfig.VERSION_CODE){
                setActive(R.string.active_restart,com.google.android.material.R.attr.colorSecondary,com.google.android.material.R.attr.colorOnSecondary,R.drawable.ic_error)
                return
            }

            setActive(R.string.active_success,com.google.android.material.R.attr.colorPrimary,com.google.android.material.R.attr.colorOnPrimary,R.drawable.ic_success)
        } else {
            setActive(R.string.active_error,com.google.android.material.R.attr.colorErrorContainer,com.google.android.material.R.attr.colorOnErrorContainer, R.drawable.ic_error)
        }
    }
    override fun onResume() {
        super.onResume()
        setMacBluetoothData()
        serverConnect()
    }

    private fun startServer(){
        stopServer()
        if(!SendWebdavServer.isRunning){
            Log.i(tag,"bluetooth server start!")
            val intent = Intent(this, SendWebdavServer::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
    }

    private fun stopServer(){
        if(SendWebdavServer.isRunning){
            Log.i(tag,"bluetooth server stop!")
            val intent = Intent(this, SendWebdavServer::class.java)
            this@MainActivity.stopService( intent)
        }
    }
    private fun syncFromServer(){
        coroutineScope .launch(Dispatchers.IO) {
            try {
                // 在后台线程中执行网络操作
                val bluetoothData  = WebdavUtils(SpUtils.getString("webdav_username", ""),SpUtils.getString("webdav_password", "")).getFromServer()
                if(bluetoothData!=null){
                    SpUtils.putString("pref_data",bluetoothData.data)
                }
                withContext(Dispatchers.Main) {
                    setMacBluetoothData()
                }
            }catch (e: SardineException){
                e.message?.let { Log.e(tag, it) }
                withContext(Dispatchers.Main) {
                    showMsg(R.string.webdav_error)
                }
            }
        }
    }


    private fun setMacBluetoothData() {
        SpUtils.getString("pref_mac", "").apply {
            Log.i(tag,"pref_mac = $this")
            binding.macLabel.text = this
        }
        SpUtils.getString("pref_data", "").apply {
            Log.i(tag,"pref_data = $this")
            binding.broadcastLabel.text = this
        }
        SpUtils.getString("pref_rssi", "").apply {
            Log.i(tag,"pref_rssi = $this")
            binding.signalLabel.text = this
        }
        SpUtils.getBoolean("pref_enable_webdav", false).apply {
            Log.i(tag,"pref_enable_webdav = $this")
            if (this) {
                binding.webdavPanel.visibility  = View.VISIBLE
                binding.senderWebdav.visibility = View.VISIBLE
            } else {
                binding.senderWebdav.visibility = View.GONE
                binding.webdavPanel.visibility  = View.GONE
            }
            binding.webdavEnable.isChecked = this

            binding.webdavEnable.setOnCheckedChangeListener { _, isChecked ->
                SpUtils.putBoolean("pref_enable_webdav", isChecked)
                if (isChecked) {
                    binding.senderWebdav.visibility = View.VISIBLE
                    binding.webdavPanel.visibility  = View.VISIBLE
                } else {
                    binding.senderWebdav.visibility = View.GONE
                    binding.webdavPanel.visibility  = View.GONE

                }
                serverConnect()
            }
        }
        //是否作为发送端
        SpUtils.getBoolean("pref_as_sender", false).apply {
            binding.asSender.isChecked = this
            binding.asSender.setOnCheckedChangeListener { _, isChecked ->
                SpUtils.putBoolean("pref_as_sender", isChecked)
                if (isChecked) {
                    binding.enable.visibility = View.GONE
                    SpUtils.putBoolean("pref_enable", false)
                } else {
                    binding.enable.visibility = View.VISIBLE
                }
                serverConnect()
            }

        }
        //是否开启模拟
        SpUtils.getBoolean("pref_enable", false).apply {
            binding.switchButton.isChecked = this
            binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
                SpUtils.putBoolean("pref_enable", isChecked)
                serverConnect()
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
                        serverConnect()
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
                        serverConnect()
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
                        serverConnect()
                    }
                })
            }
        }
        SpUtils.getString("webdav_last", getString(R.string.webdav_no_sync)).apply {
            binding.lastDate.text = this
        }

    }

    /**
     * 显示输入框
     */
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