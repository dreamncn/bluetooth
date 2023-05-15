package net.ankio.bluetooth.data

import android.content.Context

class BluetoothData(context: Context) {
    private val bluetoothCompanyParser = BluetoothCompanyParser(context)
    fun parseManufacturerData(advertisingData: ByteArray): String? {
        try{
            var currentIndex = 0
            while (currentIndex < advertisingData.size) {
                val fieldLength = advertisingData[currentIndex].toInt() and 0xFF
                if (fieldLength == 0) {
                    break
                }
                val fieldType = advertisingData[currentIndex + 1].toInt() and 0xFF
                val fieldData =
                    advertisingData.copyOfRange(currentIndex + 2, currentIndex + fieldLength + 1)
                // 解析字段类型
                when (fieldType) {
                    0xFF -> {
                        // 解析 Manufacturer Specific Data 字段
                        val companyId = parseCompanyId(fieldData, 0)
                        //fieldData.copyOfRange(2, fieldData.size)
                        // 处理厂商数据
                        return bluetoothCompanyParser.getCompanyName(companyId)
                    }
                }

                currentIndex += fieldLength + 1
            }
        }catch (_:IndexOutOfBoundsException){
            return null
        }
        return null
    }

    private fun parseCompanyId(data: ByteArray, offset: Int): Int {
        val companyIdBytes = data.copyOfRange(offset, offset + 2)
        return ((companyIdBytes[1].toInt() and 0xFF) shl 8) or (companyIdBytes[0].toInt() and 0xFF)
    }


}