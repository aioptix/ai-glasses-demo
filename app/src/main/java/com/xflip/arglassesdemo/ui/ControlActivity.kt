package com.xflip.arglassesdemo.ui

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SpinnerAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bhm.ble.utils.BleLogger
import com.xflip.arglassesdemo.App
import com.xflip.arglassesdemo.R
import com.xflip.arglassesdemo.base.BaseActivity
import com.xflip.arglassesdemo.ble.BleCommand
import com.xflip.arglassesdemo.databinding.ActivityControlBinding
import com.xflip.arglassesdemo.utils.ViewUtil
import com.xflip.arglassesdemo.vm.ControlViewModel

class ControlActivity() : BaseActivity<ControlViewModel, ActivityControlBinding>() {
    override fun createViewModel() = ControlViewModel(application)

    override fun initData() {
        super.initData()
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
    }

    private fun initLanguageSpinner() {
        val spinnerItems = arrayOf("English", "German", "Japanese", "Chinese", "French", "Korean", "Spanish")
        val languageItems = arrayOf(BleCommand.LANGUAGE_ENGLISH, BleCommand.LANGUAGE_GERMAN, BleCommand.LANGUAGE_JAPANESE,
            BleCommand.LANGUAGE_CHINESE, BleCommand.LANGUAGE_FRENCH, BleCommand.LANGUAGE_KOREAN, BleCommand.LANGUAGE_SPANISH)
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

}