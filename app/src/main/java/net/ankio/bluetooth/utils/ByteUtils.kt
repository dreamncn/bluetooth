package net.ankio.bluetooth.utils

import android.util.Log
import java.util.Locale
import kotlin.experimental.xor

/**
 *
 * @description ByteUtils
 * @author llw
 * @date 2021/9/13 14:28
 */
object ByteUtils {

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    fun hexStringToBytes(hexString: String): ByteArray {
        val hexString = hexString.uppercase(Locale.getDefault())
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val byteArrayResult = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            byteArrayResult[i] = (charToByte(hexChars[pos]).toInt()
                .shl(4) or charToByte(hexChars[pos + 1]).toInt()).toByte()
        }
        Log.d("TAG", "hexStringToBytes: " + byteArrayResult.contentToString())
        return byteArrayResult
    }

    /**
     * Convert byte[] to string
     */
    fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.isEmpty()) {
            return null
        }
        for (element in src) {
            val v = element.toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }


    fun byteToHexString(src: Byte): String = Integer.toHexString((src.toInt() and 0xFF))

    /**
     * 亦或校验(BCC校验)
     *
     * @param datas
     * @return
     */
    fun getBCCResult(datas: ByteArray): Byte {
        var temp = datas[0]
        for (i in 1 until datas.size) {
            temp = temp xor datas[i]
        }
        return temp
    }

    fun getBCCResult(hexString: String): String {
        val byteToHexString = byteToHexString(getBCCResult(hexStringToBytes(hexString)))
        return if (byteToHexString.length < 2) "0$byteToHexString" else byteToHexString
    }

    fun byteArrayToHexString(byteArray: ByteArray): String {
        val sb = StringBuilder()
        for (i in byteArray.indices) {
            var hex = Integer.toHexString((byteArray[i]).toInt() and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
                sb.append(hex.uppercase(Locale.getDefault()))
            }
        }
        return sb.toString()
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private fun charToByte(c: Char): Byte = "0123456789ABCDEF".indexOf(c).toByte()

}