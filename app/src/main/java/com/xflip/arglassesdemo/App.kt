package com.xflip.arglassesdemo

import android.app.Application
import android.util.SparseArray
import com.bhm.ble.BleManager
import com.bhm.ble.attribute.BleOptions
import com.bhm.ble.callback.BleConnectCallback
import com.bhm.ble.callback.BleScanCallback
import com.bhm.ble.data.BleDescriptorGetType
import com.bhm.ble.data.Constants
import com.bhm.ble.device.BleDevice
import com.bhm.ble.utils.BleLogger
import com.bhm.ble.utils.BleUtil
import com.xflip.arglassesdemo.ble.BleCommand
import com.xflip.arglassesdemo.entity.RefreshBleDevice
import kotlinx.coroutines.flow.MutableStateFlow

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }


    /*----------------------------- Bluetooth -------------------------------*/
    fun initBle(refreshBleDevice: MutableStateFlow<RefreshBleDevice>) {
        BleManager.get().init(
            instance,
            BleOptions.Builder()
                .setScanMillisTimeOut(5000)
                .setConnectMillisTimeOut(5000)
                .setMtu(180, false)
                .setMaxConnectNum(2)
                .setConnectRetryCountAndInterval(1, 1000)
                .build()
        )
        BleManager.get().registerBluetoothStateReceiver {
            onStateOff {
                refreshBleDevice.value = RefreshBleDevice(null, System.currentTimeMillis())
            }
        }
    }

    fun writeData(bleDevice: BleDevice,
                  data: ByteArray) {
        BleLogger.i("data is: ${BleUtil.bytesToHex(data)}")
        val mtu = BleManager.get().getOptions()?.mtu?: Constants.DEFAULT_MTU
        // The mtu length includes one byte of ATT's opcode and two bytes of ATT's handle.
        val maxLength = mtu - 3
        val listData: SparseArray<ByteArray> = BleUtil.subpackage(data, maxLength)
        BleManager.get().writeData(bleDevice, BleCommand.SERVICE_UUID, BleCommand.WRITE_CHARACTERISTIC, listData) {
            onWriteFail { _, currentPackage, _, t ->
                BleLogger.w("The${currentPackage}package data was written fail：${t.message}")
            }
            onWriteSuccess { _, currentPackage, _, justWrite ->
                BleLogger.d("${BleCommand.WRITE_CHARACTERISTIC} -> The${currentPackage}package data was written successfully：" +
                        BleUtil.bytesToHex(justWrite))
            }
            onWriteComplete { _, allSuccess ->
                // Indicates that all data is written successfully,
                // and the successful logic can be processed in this method.
                BleLogger.d("${BleCommand.WRITE_CHARACTERISTIC} -> Writing data is completed, successful：$allSuccess")
            }
        }
    }

    fun notify(bleDevice: BleDevice) {
        BleManager.get().notify(bleDevice, BleCommand.SERVICE_UUID, BleCommand.NOTIFY_CHARACTERISTIC, BleDescriptorGetType.AllDescriptor) {
            onNotifyFail { _, _, t ->
                BleLogger.w("notify fail：${t.message}")
            }
            onNotifySuccess { _, notifyUUID ->
                BleLogger.d("notify success：${notifyUUID}")
            }
            onCharacteristicChanged {_, notifyUUID, data ->
                // Data processing is in the IO thread,
                // and the display UI needs to be switched to the main thread.
                launchInMainThread {
                    BleLogger.d("Notify receive${notifyUUID}data：" + BleUtil.bytesToHex(data))
                }
            }
        }
    }

    fun stopNotify(bleDevice: BleDevice) {
        val success = BleManager.get().stopNotify(bleDevice, BleCommand.SERVICE_UUID, BleCommand.NOTIFY_CHARACTERISTIC, BleDescriptorGetType.AllDescriptor)
        if (success == true) {
            BleLogger.d("notify stop success：${BleCommand.NOTIFY_CHARACTERISTIC}")
        } else {
            BleLogger.w("notify stop fail：${BleCommand.NOTIFY_CHARACTERISTIC}")
        }
    }

    fun startScanAndConnect(scanCallback: BleScanCallback.() -> Unit, connectCallback: BleConnectCallback.() -> Unit) {
        BleManager.get().startScanAndConnect(
            false,
            scanCallback,
            connectCallback
        )
    }

    fun disConnect(bleDevice: BleDevice?) {
        bleDevice?.let { device ->
            BleManager.get().disConnect(device)
        }
    }

    fun close() {
        BleManager.get().closeAll()
    }

    fun connect(bleDevice: BleDevice?, connectCallback: BleConnectCallback.() -> Unit) {
        bleDevice?.let { device ->
            BleManager.get().connect(device, false, connectCallback)
        }
    }

}