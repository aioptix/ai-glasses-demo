package com.xflip.arglassesdemo

import android.Manifest
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

val LOCATION_PERMISSION = if (VERSION.SDK_INT < VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
} else {
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
    )
}