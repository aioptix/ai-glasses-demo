package com.xflip.arglassesdemo.ui

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bhm.ble.utils.BleLogger
import com.xflip.arglassesdemo.App
import com.xflip.arglassesdemo.base.BaseActivity
import com.xflip.arglassesdemo.ble.BleCommand
import com.xflip.arglassesdemo.databinding.ActivityAiQaBinding
import com.xflip.arglassesdemo.utils.ViewUtil
import com.xflip.arglassesdemo.vm.AiQaViewModel

class AiQaActivity : BaseActivity<AiQaViewModel, ActivityAiQaBinding>() {

    var firstEnterPage = false
    override fun createViewModel() = AiQaViewModel(application)

    override fun initData() {
        super.initData()
        initListener()
        initModeSpinner()
    }

    private fun initListener() {
        viewBinding.btnQuestion.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            sendQuestionToGlasses()
        }
        viewBinding.btnReply.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            sendReplyToGlasses()
        }
        viewBinding.switchQuestionShow.setOnCheckedChangeListener { _, isChecked ->
            if (App.instance.getBleDevice() == null) {
                return@setOnCheckedChangeListener
            }
            App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.glassesControl(BleCommand.FUNCTION_AI_QUESTION_DIALOG_SHOW,
                if (isChecked) BleCommand.CONTROL_OPEN else BleCommand.CONTROL_CLOSE))
        }
    }

    val spinnerItems = arrayOf(
        "Read and write mode", "Driving mode", "Gaming mode", "Translation mode",
        "Cooking mode", "Morse code mode", "Team mode", "Yoga mode", "ChatGPT mode", "Phone mode")
    val modeItems = arrayOf(
        BleCommand.AI_READING_AND_WRITING_MODE, BleCommand.AI_DRIVING_MODE, BleCommand.AI_GAMING_MODE,
        BleCommand.AI_TRANSLATION_MODE, BleCommand.AI_COOKING_MODE, BleCommand.AI_MORSE_CODE_MODE,
        BleCommand.AI_TEAM_MODE, BleCommand.AI_YOGA_MODE, BleCommand.AI_CHATGPT_MODE, BleCommand.AI_PHONE_MODE)

    private fun initModeSpinner() {
        val spinnerAdapter = ArrayAdapter(App.instance, android.R.layout.simple_spinner_item, spinnerItems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spinnerMode.adapter = spinnerAdapter
        viewBinding.spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                BleLogger.d("Select Language: ${spinnerItems[position]}, code: ${modeItems[position]}")
                if (App.instance.getBleDevice() == null) {
                    BleLogger.e("App.instance.getBleDevice() == null")
                    return
                }
                App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.setAI(false, modeItems[position]))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

        }
    }

    private fun sendQuestionToGlasses() {
        if (viewBinding.etQuestion.text.isBlank()) {
            Toast.makeText(application, "Please input Question!!", Toast.LENGTH_SHORT).show()
            return
        }
        if (App.instance.getBleDevice() == null) {
            BleLogger.e("App.instance.getBleDevice() == null")
            return
        }
        val question = viewBinding.etQuestion.text.toString()
        for (data in BleCommand.aiQuestion(question)) {
            App.instance.writeData(App.instance.getBleDevice()!!, data)
        }
    }

    private fun sendReplyToGlasses() {
        if (viewBinding.etReply.text.isBlank()) {
            Toast.makeText(application, "Please input Reply!!", Toast.LENGTH_SHORT).show()
            return
        }
        if (App.instance.getBleDevice() == null) {
            BleLogger.e("App.instance.getBleDevice() == null")
            return
        }
        val question = viewBinding.etReply.text.toString()
        for (data in BleCommand.aiReply(question)) {
            App.instance.writeData(App.instance.getBleDevice()!!, data)
        }
    }

}