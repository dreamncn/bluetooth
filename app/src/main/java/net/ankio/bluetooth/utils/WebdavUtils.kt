package net.ankio.bluetooth.utils

import com.github.sardine.Sardine
import com.github.sardine.SardineFactory
import com.google.gson.Gson

import net.ankio.bluetooth.BuildConfig
import net.ankio.bluetooth.bluetooth.BluetoothData
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class WebdavUtils {


    private val sardine: Sardine = SardineFactory.begin(
        SpUtils.getString("webdav_username", ""),
        SpUtils.getString("webdav_password", "")
    )
    private val server = SpUtils.getString("webdav_server", "https://dav.jianguoyun.com/dav/")
    private var dir = server + "/" + BuildConfig.APPLICATION_ID + "/"
    var file = "$dir/bluetooth.json"

    init {
        if (!sardine.exists(dir)) {
            sardine.createDirectory(dir)
        }
    }

    fun sendToServer(bluetoothData: BluetoothData) {


        val gson = Gson()


        // 将 User 对象转换为 JSON 数据
        val json = gson.toJson(bluetoothData)
        if (sardine.exists(file))
            sardine.delete(file)
        // 输出 JSON 数据
        sardine.put("$dir/bluetooth.json", json.toByteArray())

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