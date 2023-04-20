package net.ankio.bluetooth.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class BluetoothCompany(
    @SerializedName("code") val code: Int,
    @SerializedName("name") val name: String
)

class BluetoothCompanyParser(context: Context) {
    private val gson = Gson()
    private val json: String =
        context.assets.open("company_ids.json").bufferedReader().use { it.readText() }

    private val companies: List<BluetoothCompany> = gson.fromJson(
        json,
        Array<BluetoothCompany>::class.java
    ).toList()

    fun getCompanyName(code: Int): String? {
        val company = companies.find { it.code == code }
        return company?.name
    }
}
