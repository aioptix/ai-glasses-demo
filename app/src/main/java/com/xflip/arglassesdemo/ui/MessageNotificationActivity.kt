package com.xflip.arglassesdemo.ui

import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bhm.ble.utils.BleLogger
import com.xflip.arglassesdemo.App
import com.xflip.arglassesdemo.R
import com.xflip.arglassesdemo.base.BaseActivity
import com.xflip.arglassesdemo.ble.BleCommand
import com.xflip.arglassesdemo.databinding.ActivityMessageNotifcationBinding
import com.xflip.arglassesdemo.utils.ViewUtil
import com.xflip.arglassesdemo.vm.MessageNotificationViewModel

class MessageNotificationActivity : BaseActivity<MessageNotificationViewModel, ActivityMessageNotifcationBinding>() {
    override fun createViewModel() = MessageNotificationViewModel(application)

    override fun initData() {
        super.initData()
        initListener()
        initSoftwareSpinner()
        initStyleSpinner()
    }

    private fun initListener() {
        viewBinding.btnNotifyGlasses.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            sendNotifyToGlasses()
        }
    }

    private val softwareSpinnerItems = arrayOf(
        "incoming call", "SMS", "Skype", "Line", "kakaotalk", "facebook", "twitter(X)", "whatsapp",
        "linkedin", "viber", "instagram", "messenger", "Wechat", "QQ", "DingDing", "Other")
    private val softwareItems = arrayOf(
        BleCommand.BELONG_SOFTWARE_CALL, BleCommand.BELONG_SOFTWARE_MESSAGE, BleCommand.BELONG_SOFTWARE_SKYPE,
        BleCommand.BELONG_SOFTWARE_LINE, BleCommand.BELONG_SOFTWARE_KAKAOTALK, BleCommand.BELONG_SOFTWARE_FACEBOOK,
        BleCommand.BELONG_SOFTWARE_X, BleCommand.BELONG_SOFTWARE_WHATSAPP, BleCommand.BELONG_SOFTWARE_LINKEDIN,
        BleCommand.BELONG_SOFTWARE_VIBER, BleCommand.BELONG_SOFTWARE_INSTAGRAM, BleCommand.BELONG_SOFTWARE_MESSENGER,
        BleCommand.BELONG_SOFTWARE_WECHAT, BleCommand.BELONG_SOFTWARE_QQ, BleCommand.BELONG_SOFTWARE_DINGDING, BleCommand.BELONG_SOFTWARE_OTHER)

    private var selectSoftwareIndex = -1;

    private fun initSoftwareSpinner() {
        val spinnerAdapter = ArrayAdapter(App.instance, android.R.layout.simple_spinner_item, softwareSpinnerItems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spinnerSoftware.adapter = spinnerAdapter
        viewBinding.spinnerSoftware.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectSoftwareIndex = position;
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

        }
    }

    private val styleSpinnerItems = arrayOf("Default", "Style one", "Style two", "Style three")
    val styleItems = arrayOf(
        BleCommand.NOTIFY_MESSAGE_STYLE_DEFAULT,
        BleCommand.NOTIFY_MESSAGE_STYLE_ONE,
        BleCommand.NOTIFY_MESSAGE_STYLE_TWO,
        BleCommand.NOTIFY_MESSAGE_STYLE_THREE)

    private var selectStyleIndex = -1;

    private fun initStyleSpinner() {
        val spinnerAdapter = ArrayAdapter(App.instance, android.R.layout.simple_spinner_item, styleSpinnerItems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spinnerStyle.adapter = spinnerAdapter
        viewBinding.spinnerStyle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectStyleIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

        }
    }

    private fun sendNotifyToGlasses() {
        if (viewBinding.etTitle.text.isBlank()) {
            Toast.makeText(application, "Please Input Title!!", Toast.LENGTH_SHORT).show()
            return
        }
        if (viewBinding.etContent.text.isBlank()) {
            Toast.makeText(application, "Please Input Content!!", Toast.LENGTH_SHORT).show()
            return
        }
        if (viewBinding.spinnerSoftware.selectedItemId == AdapterView.INVALID_ROW_ID || selectSoftwareIndex == -1) {
            Toast.makeText(application, "Please Select software!!", Toast.LENGTH_SHORT).show()
            return
        }
        if (viewBinding.spinnerStyle.selectedItemId == AdapterView.INVALID_ROW_ID || selectStyleIndex == -1) {
            Toast.makeText(application, "Please Select style!!", Toast.LENGTH_SHORT).show()
            return
        }
        if (App.instance.getBleDevice() == null) {
            BleLogger.e("App.instance.getBleDevice() == null")
            return
        }
        val title = viewBinding.etTitle.text.toString()
        val content = viewBinding.etContent.text.toString()
        for (data in BleCommand.setNotifyMessage(title, content,
            softwareItems[selectSoftwareIndex], getCurrentCustomTime(), styleItems[selectStyleIndex]
        )) {
            App.instance.writeData(App.instance.getBleDevice()!!, data)
        }


    }

    private fun getCurrentCustomTime():String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)-2000
        val month = getTwoDigitString(calendar.get(Calendar.MONTH))
        val day = getTwoDigitString(calendar.get(Calendar.DAY_OF_MONTH))
        val hour = getTwoDigitString(calendar.get(Calendar.HOUR_OF_DAY))
        val min = getTwoDigitString(calendar.get(Calendar.MINUTE))
        val sec = getTwoDigitString(calendar.get(Calendar.SECOND))
        return "$year" + month + day + hour + min + sec
    }

    private fun getTwoDigitString(data: Int):String {
        if (data < 10) {
            return "0$data"
        } else if (data > 99) {
            val ds = data.toString()
            return ds.substring(ds.lastIndex-2, ds.lastIndex)
        }
        return data.toString()
    }

}