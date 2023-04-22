package net.ankio.bluetooth.utils


import android.util.Log
import com.google.gson.Gson
import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import net.ankio.bluetooth.BuildConfig
import net.ankio.bluetooth.R
import net.ankio.bluetooth.bluetooth.BluetoothData
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WebdavUtils(username:String,password:String) {


    private val sardine: Sardine = OkHttpSardine()

    private val server = SpUtils.getString("webdav_server", "https://dav.jianguoyun.com/dav/").trimEnd('/')
    private var dir = server +   "/bluetooth".trimEnd('/')
    private var file = "$dir/bluetooth.json"
    init {
        sardine.setCredentials(username,password)
        if (!sardine.exists(dir)) {
            sardine.createDirectory(dir)
        }
    }

    fun sendToServer(bluetoothData: BluetoothData) {
        Log.i("Webdav","Send Bluetooth to Webdav")
        SpUtils.putString("webdav_last",getTime())
        val gson = Gson()
        // 将 User 对象转换为 JSON 数据
        val json = gson.toJson(bluetoothData)
        if (sardine.exists(file))
            sardine.delete(file)
        // 输出 JSON 数据
        sardine.put("$dir/bluetooth.json", json.toByteArray())

    }

    private fun getTime(): String {
        val currentTime = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(currentTime)
    }


    fun getFromServer(): BluetoothData? {
        if (sardine.exists(file)) {
            return Gson().fromJson(
                convertInputStreamToString(sardine.get(file)),
                BluetoothData::class.java
            )
        }
        return null
    }

    private fun convertInputStreamToString(inputStream: InputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        bufferedReader.close()
        return stringBuilder.toString()
    }


}