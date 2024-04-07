package com.xflip.arglassesdemo.vm

import android.app.Application
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.bhm.ble.BleManager
import com.bhm.ble.callback.BleConnectCallback
import com.bhm.ble.callback.BleScanCallback
import com.bhm.ble.data.BleConnectFailType
import com.bhm.ble.data.BleScanFailType
import com.bhm.ble.device.BleDevice
import com.bhm.ble.utils.BleLogger
import com.bhm.ble.utils.BleUtil
import com.xflip.arglassesdemo.App
import com.xflip.arglassesdemo.LOCATION_PERMISSION
import com.xflip.arglassesdemo.base.BaseActivity
import com.xflip.arglassesdemo.base.BaseViewModel
import com.xflip.arglassesdemo.entity.MessageEvent
import com.xflip.arglassesdemo.entity.RefreshBleDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel(private val application: Application) : BaseViewModel(application) {

    private val listDRMutableStateFlow = MutableStateFlow(
        BleDevice(null, null, null, null, null, null, null)
    )

    val listDRStateFlow: StateFlow<BleDevice> = listDRMutableStateFlow

    val listDRData = mutableListOf<BleDevice>()

    private val scanStopMutableStateFlow = MutableStateFlow(true)

    val scanStopStateFlow: StateFlow<Boolean> = scanStopMutableStateFlow

    private val refreshMutableStateFlow = MutableStateFlow(
        RefreshBleDevice(null, null)
    )

    val refreshStateFlow: StateFlow<RefreshBleDevice?> = refreshMutableStateFlow

    fun initBle() {
        App.instance.initBle(refreshMutableStateFlow)
    }

    private suspend fun hasScanPermission(activity: BaseActivity<*, *>): Boolean {
        val isBleSupport = BleManager.get().isBleSupport()
        BleLogger.e("The device support Bluetooth: $isBleSupport")
        if (!isBleSupport) {
            return false
        }
        var hasScanPermission = suspendCoroutine { continuation ->
            activity.requestPermission(
                LOCATION_PERMISSION,
                {
                    BleLogger.d("Obtained permission")
                    continuation.resume(true)
                }, {
                    BleLogger.w("Missing permissions")
                    continuation.resume(false)
                }
            )
        }
        //If the GPS of some devices is turned off, after applying for positioning permission,
        // the GPS will still be turned off.
        // Here you need to jump to the page based on whether the GPS is turned on.
        if (hasScanPermission && !BleUtil.isGpsOpen(application)) {
            // Jump to the system GPS settings page. GPS settings are global and independent.
            // Whether it is turned on or not has nothing to do with permission application.
            hasScanPermission = suspendCoroutine {
                activity.startActivity(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                ) { _, _ ->
                    val enable = BleUtil.isGpsOpen(application)
                    BleLogger.i("GPS turned on: $enable")
                    it.resume(enable)
                }
            }
        }
        if (hasScanPermission && !BleManager.get().isBleEnable()) {
            hasScanPermission = suspendCoroutine {
                activity.startActivity(
                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                ) { _, _ ->
                    viewModelScope.launch {
                        // After turning on Bluetooth, it takes some time to get the enabled status.
                        // Here is a delay.
                        delay(1000)
                        val enable = BleManager.get().isBleEnable()
                        BleLogger.i("BLE turned on: $enable")
                        it.resume(enable)
                    }
                }
            }
        }
        return hasScanPermission
    }

    fun startScan(activity: BaseActivity<*, *>) {
        viewModelScope.launch {
            val hasScanPermission = hasScanPermission(activity)
            if (hasScanPermission) {
                BleManager.get().startScan(getScanCallback(true))
            } else {
                BleLogger.e("Please check permissions, check GPS switch, check Bluetooth switch")
            }
        }
    }

    private fun getScanCallback(showData: Boolean): BleScanCallback.() -> Unit {
        return {
            onScanStart {
                BleLogger.d("onScanStart")
                scanStopMutableStateFlow.value = false
            }
            onLeScan { bleDevice, _ ->
                // You can clear the list data based on currentScanCount.
                bleDevice.deviceName?.let { _ ->

                }
            }
            onLeScanDuplicateRemoval { bleDevice, _ ->
                bleDevice.deviceName?.let { _ ->
                    if (showData) {
                        listDRData.add(bleDevice)
                        listDRMutableStateFlow.value = bleDevice
                    }
                }
            }
            onScanComplete { bleDeviceList, bleDeviceDuplicateRemovalList ->
                // The scanned data is the sum of all scan times
                bleDeviceList.forEach {
                    it.deviceName?.let { deviceName ->
                        BleLogger.i("bleDeviceList-> $deviceName, ${it.deviceAddress}")
                    }
                }
                bleDeviceDuplicateRemovalList.forEach {
                    it.deviceName?.let { deviceName ->
                        BleLogger.e("bleDeviceDuplicateRemovalList-> $deviceName, ${it.deviceAddress}")
                    }
                }
                scanStopMutableStateFlow.value = true
                if (listDRData.isEmpty() && showData) {
                    Toast.makeText(application, "No data was scanned", Toast.LENGTH_SHORT).show()
                }
            }
            onScanFail {
                val msg: String = when (it) {
                    is BleScanFailType.UnSupportBle -> "Device does not support Bluetooth"
                    is BleScanFailType.NoBlePermission -> "Insufficient permissions, please check"
                    is BleScanFailType.GPSDisable -> "The device does not turn on GPS positioning"
                    is BleScanFailType.BleDisable -> "Bluetooth is not turned on"
                    is BleScanFailType.AlReadyScanning -> "Scanning"
                    is BleScanFailType.ScanError -> {
                        "${it.throwable?.message}"
                    }
                }
                BleLogger.e(msg)
                Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
                scanStopMutableStateFlow.value = true
            }
        }
    }

    fun stopScan() {
        BleManager.get().stopScan()
    }

    fun isConnected(bleDevice: BleDevice?) = BleManager.get().isConnected(bleDevice)

    fun connect(address: String) {
        connect(BleManager.get().buildBleDeviceByDeviceAddress(address))
    }

    fun startScanAndConnect(activity: BaseActivity<*, *>) {
        viewModelScope.launch {
            val hasScanPermission = hasScanPermission(activity)
            if (hasScanPermission) {
                App.instance.startScanAndConnect(getScanCallback(false), connectCallback)
            }
        }
    }

    fun connect(bleDevice: BleDevice?) {
        App.instance.connect(bleDevice, connectCallback)
    }

    private val connectCallback: BleConnectCallback.() -> Unit = {
        onConnectStart {
            BleLogger.e("-----onConnectStart")
        }
        onConnectFail { bleDevice, connectFailType ->
            val msg: String = when (connectFailType) {
                is BleConnectFailType.UnSupportBle -> "Device does not support Bluetooth"
                is BleConnectFailType.NoBlePermission -> "Insufficient permissions, please check"
                is BleConnectFailType.NullableBluetoothDevice -> "Device is empty"
                is BleConnectFailType.BleDisable -> "Bluetooth is not turned on"
                is BleConnectFailType.ConnectException -> "Connection abnormality(${connectFailType.throwable.message})"
                is BleConnectFailType.ConnectTimeOut -> "Connection timed out"
                is BleConnectFailType.AlreadyConnecting -> "connecting"
                is BleConnectFailType.ScanNullableBluetoothDevice -> "Connection failed, scan data is empty"
            }
            BleLogger.e(msg)
            Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
        }
        onDisConnecting { isActiveDisConnected, bleDevice, _, _ ->
            BleLogger.e("-----${bleDevice.deviceAddress} -> onDisConnecting: $isActiveDisConnected")
        }
        onDisConnected { isActiveDisConnected, bleDevice, _, _ ->
            Toast.makeText(application, "Disconnect(${bleDevice.deviceAddress}，isActiveDisConnected: " +
                    "$isActiveDisConnected)", Toast.LENGTH_SHORT).show()
            BleLogger.e("-----${bleDevice.deviceAddress} -> onDisConnected: $isActiveDisConnected")
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
            // Send disconnect Message
            val message = MessageEvent()
            message.data = bleDevice
            EventBus.getDefault().post(message)
        }
        onConnectSuccess { bleDevice, _ ->
            Toast.makeText(application, "connection succeeded(${bleDevice.deviceAddress})", Toast.LENGTH_SHORT).show()
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
            App.instance.notify(bleDevice)
        }
    }

    fun disConnect(bleDevice: BleDevice?) {
        App.instance.disConnect(bleDevice)
    }

    fun close() {
        App.instance.close()
    }

}