package net.ankio.bluetooth.bluetooth

/**
 *
 * @description BleDevice
 * @author llw
 * @date 2021/9/10 11:29
 */
data class BleDevice(
    var data: String,
    var company: String?,
    var rssi: Int,
    var address: String,
    var name: String?
)