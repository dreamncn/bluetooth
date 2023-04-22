package net.ankio.bluetooth.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ankio.bluetooth.R
import net.ankio.bluetooth.utils.ByteUtils
import net.ankio.bluetooth.utils.SpUtils
import net.ankio.bluetooth.utils.WebdavUtils

@SuppressWarnings("MissingPermission")
class SendWebdavServer : Service() {
    companion object {
        var isRunning = false
        private const val CHANNEL_ID = "ForegroundServiceChannel"
    }
    private val TAG = "BluetoothScanService"
    private var deviceAddress = SpUtils.getString("pref_mac", "") // 指定的蓝牙设备MAC地址

    private val scanInterval: Long = 10 * 60 * 1000 // 10 minutes

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanCallback: ScanCallback
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startScan()
        return START_STICKY
    }
    override fun onCreate() {

        super.onCreate()
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_bluetooth_scan)
            .setContentText(getString(R.string.server_name))
            .build()

        startForeground(1, notification)
        if(deviceAddress===""){
            Log.i(TAG,"You need to set mac first.")
            stopSelf()
        }
        bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

        //扫描结果回调
       scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                deviceAddress = SpUtils.getString("pref_mac", "")
                if(result.device.address == deviceAddress){
                    stopScan()
                    Log.i(TAG, "Found device: $deviceAddress")
                    val scanRecord = result.scanRecord?.bytes ?: return
                    val coroutineScope = CoroutineScope(Dispatchers.Main)
                    coroutineScope .launch(Dispatchers.IO) {
                        WebdavUtils(
                            SpUtils.getString("webdav_username", ""),
                            SpUtils.getString("webdav_password", "")
                        ).sendToServer(
                            net.ankio.bluetooth.bluetooth.BluetoothData(
                                ByteUtils.byteArrayToHexString(scanRecord),
                                deviceAddress,
                                "-50"
                            )
                        )
                    }
                }else{
                    Log.i(TAG, "Device: ${result.device.address}")
                }


            }
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
        Log.i(TAG, "Server destroy")
        isRunning = false
    }

     private fun startScan() {
        if (bluetoothAdapter.isEnabled) {
            Log.i(TAG, "Start scanning...")
            bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
            Handler(Looper.getMainLooper()).postDelayed({
                startScan()
            },scanInterval )
        } else {
            Log.e(TAG, getString(R.string.unsupported_bluetooth))
            stopSelf()
        }
    }




    private fun stopScan() {
        Log.i(TAG, "Stop scanning")
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_ID
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }
}