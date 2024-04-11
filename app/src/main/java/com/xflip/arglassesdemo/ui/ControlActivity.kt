package com.xflip.arglassesdemo.ui

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.bhm.ble.utils.BleLogger
import com.bhm.ble.utils.BleUtil
import com.xflip.arglassesdemo.App
import com.xflip.arglassesdemo.base.BaseActivity
import com.xflip.arglassesdemo.ble.BleCommand
import com.xflip.arglassesdemo.databinding.ActivityControlBinding
import com.xflip.arglassesdemo.entity.MessageEvent
import com.xflip.arglassesdemo.utils.ViewUtil
import com.xflip.arglassesdemo.vm.ControlViewModel
import java.nio.charset.Charset

class ControlActivity() : BaseActivity<ControlViewModel, ActivityControlBinding>() {

    var firstEnterPage = false
    override fun createViewModel() = ControlViewModel(application)

    override fun initData() {
        super.initData()
        sendGetDeviceInfoCommand()
        initListener()
        initLanguageSpinner()
    }

    private fun initListener() {
        viewBinding.seekBarBrightness.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (App.instance.getBleDevice() != null) {
                    App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.adjustBrightness(progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // nothing
            }

        })
        viewBinding.seekBarVolume.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (App.instance.getBleDevice() != null) {
                    App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.adjustVolume(progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // nothing
            }

        })
        viewBinding.btnSubmitTurnOffTime.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            val time = viewBinding.editScreenTurnOffTime.text.toString()
            val turnOffTime = Integer.parseInt(time)
            if (App.instance.getBleDevice() == null) {
                return@setOnClickListener
            }
            App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.setTurnOffScreenTime(turnOffTime))
        }
        viewBinding.btnQA.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            startActivity(Intent(this@ControlActivity, AiQaActivity::class.java))
        }
        viewBinding.btnNotification.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            startActivity(Intent(this@ControlActivity, MessageNotificationActivity::class.java))
        }
    }

    val spinnerItems = arrayOf("English", "German", "Japanese", "Chinese", "French", "Korean", "Spanish")
    val languageItems = arrayOf(BleCommand.LANGUAGE_ENGLISH, BleCommand.LANGUAGE_GERMAN, BleCommand.LANGUAGE_JAPANESE,
        BleCommand.LANGUAGE_CHINESE, BleCommand.LANGUAGE_FRENCH, BleCommand.LANGUAGE_KOREAN, BleCommand.LANGUAGE_SPANISH)

    private fun initLanguageSpinner() {
        val spinnerAdapter = ArrayAdapter(App.instance, android.R.layout.simple_spinner_item, spinnerItems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spinnerLanguage.adapter = spinnerAdapter
        viewBinding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (!firstEnterPage) {
                    firstEnterPage = true
                    return
                }
                BleLogger.d("Select Language: ${spinnerItems[position]}, code: ${languageItems[position]}")
                if (App.instance.getBleDevice() == null) {
                    BleLogger.e("App.instance.getBleDevice() == null")
                    return
                }
                App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.setLanguage(languageItems[position]))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

        }
    }

    override fun onMessageEvent(event: MessageEvent?) {
        super.onMessageEvent(event)
        if (event?.msg == App.NOTIFY_DATA) {
            if (event.data is ByteArray) {
                processData(event.data as ByteArray)
            }
        }
    }

    private fun processData(data: ByteArray) {
        if (!verifyCorrectPacket(data)) {
            return
        }
        val useData = data.copyOfRange(6, data.lastIndex)
        if (useData[0] == (0xD9).toByte()) {
            val name = String(useData.copyOfRange(1, 8), Charsets.UTF_8)
            val station = useData[9]
            val type = useData[10]

            val currentBrightness = useData[13]
            val currentLanguage = useData[14]
            val currentVolume = useData[15]

            initSetting(currentBrightness.toInt(), currentLanguage, currentVolume.toInt())

            BleLogger.d("Version Name: $name")
        }
    }

    private fun initSetting(brightness: Int, language: Byte, volume: Int) {
        viewBinding.seekBarBrightness.progress = brightness
        viewBinding.seekBarVolume.progress = volume
        val selectItem = languageItems.indexOf(language)
        viewBinding.spinnerLanguage.setSelection(selectItem)
    }

    private fun verifyCorrectPacket(data: ByteArray): Boolean {
        if (data[0] != (0xAE).toByte()) return false
        val serializerNumber = data[1]
        val bleVersion = data[2]
        val payloadLength = data[3]
        val crc16First = data[4]
        val crc16Last = data[5]
        val crc16Data = data.copyOfRange(6, data.lastIndex)
        if (crc16Data.size != (payloadLength+1)) {
            BleLogger.e("crc16 data not right!! crc16 data size is ${crc16Data.size}, payload length is ${payloadLength.toInt()}")
            return false
        }
        val crc16 = BleCommand.CRC16_IBM(crc16Data)
        val willCheckCrc16First = ((crc16 shr 8) and 0xFF).toByte()
        val willCheckCrc16Last =  (crc16 and 0xFF).toByte()
        if (crc16First != willCheckCrc16First || crc16Last != willCheckCrc16Last) {
            BleLogger.e("crc16 first: ${getHexString(crc16First)}, will check: ${getHexString(willCheckCrc16First)}; " +
                    "crc16 last: ${getHexString(crc16Last)}, will check: ${getHexString(willCheckCrc16Last)}")
            return false
        }
        return true
    }

    private fun getHexString(data: Byte): String {
        return Integer.toHexString(data.toInt() and 0xFF)
    }

    private fun sendGetDeviceInfoCommand() {
        if (App.instance.getBleDevice() == null) {
            BleLogger.e("App.instance.getBleDevice() == null")
            return
        }
        App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.getDeviceInfo())
    }

}