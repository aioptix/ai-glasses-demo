package com.xflip.arglassesdemo.entity

import com.bhm.ble.device.BleDevice

data class RefreshBleDevice(
    val bleDevice: BleDevice?,
    var tag: Long? = null
)