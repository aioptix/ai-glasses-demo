package com.xflip.arglassesdemo.ui

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bhm.ble.utils.BleLogger
import com.xflip.arglassesdemo.App
import com.xflip.arglassesdemo.base.BaseActivity
import com.xflip.arglassesdemo.ble.BleCommand
import com.xflip.arglassesdemo.databinding.ActivityNavBinding
import com.xflip.arglassesdemo.utils.ViewUtil
import com.xflip.arglassesdemo.vm.NavViewModel

class NavActivity : BaseActivity<NavViewModel, ActivityNavBinding>() {
    override fun createViewModel() = NavViewModel(application)

    override fun initData() {
        super.initData()
        initListener()
        initFirstRoadSegmentSpinner()
        initSecondRoadSegmentSpinner()
    }

    private fun initListener() {
        viewBinding.btnSendCommand.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            checkTheDataAndSendCommand()
        }

        viewBinding.btnOpenNav.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            if (App.instance.getBleDevice() == null) {
                BleLogger.e("App.instance.getBleDevice() == null")
                return@setOnClickListener
            }
            App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.glassesControl(BleCommand.FUNCTION_NAV_OPEN_OR_CLOSE, BleCommand.CONTROL_OPEN))
        }

        viewBinding.btnCloseNav.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            if (App.instance.getBleDevice() == null) {
                BleLogger.e("App.instance.getBleDevice() == null")
                return@setOnClickListener
            }
            App.instance.writeData(App.instance.getBleDevice()!!, BleCommand.glassesControl(BleCommand.FUNCTION_NAV_OPEN_OR_CLOSE, BleCommand.CONTROL_CLOSE))
        }

    }

    private fun checkTheDataAndSendCommand() {
        val direction = viewBinding.etDirection.text
        if (direction.isBlank()) {
            Toast.makeText(application, "Please input direction number!", Toast.LENGTH_SHORT).show()
            return
        }
        var directionInt = direction.toString().toInt()

        val totalTime = viewBinding.etTotalTime.text
        if (totalTime.isBlank()) {
            Toast.makeText(application, "Please input Total Time!", Toast.LENGTH_SHORT).show()
            return
        }
        val totalTimeInt = totalTime.toString().toInt()

        val totalDistance = viewBinding.etTotalDistance.text
        if (totalDistance.isBlank()) {
            Toast.makeText(application, "Please input Total Distance!", Toast.LENGTH_SHORT).show()
            return
        }
        val totalDistanceInt = totalDistance.toString().toInt()

        val remainingDistance = viewBinding.etRemainDistance.text
        if (remainingDistance.isBlank()) {
            Toast.makeText(application, "Please input Remaining distance!", Toast.LENGTH_SHORT).show()
            return
        }
        val remainingDistanceInt = remainingDistance.toString().toInt()

        val currentSpeed = viewBinding.etCurrentSpeed.text
        if (currentSpeed.isBlank()) {
            Toast.makeText(application, "Please input Current Speed!", Toast.LENGTH_SHORT).show()
            return
        }
        val currentSpeedInt = currentSpeed.toString().toInt()

        val remainingDistanceOfCurrentRoadSegment = viewBinding.etCurrentRoadSegment.text
        if (remainingDistanceOfCurrentRoadSegment.isBlank()) {
            Toast.makeText(application, "Please input  remaining distance of current road segment!", Toast.LENGTH_SHORT).show()
            return
        }
        val remainingDistanceOfCurrentRoadSegmentInt = remainingDistanceOfCurrentRoadSegment.toString().toInt()

        val firstRoadSegment = viewBinding.etFirstRoadSegment.text
        if (firstRoadSegment.isBlank()) {
            Toast.makeText(application, "Please input First Road Segment distance!", Toast.LENGTH_SHORT).show()
            return
        }
        val firstRoadSegmentInt = firstRoadSegment.toString().toInt()

        val secondRoadSegment = viewBinding.etSecondRoadSegment.text
        if (secondRoadSegment.isBlank()) {
            Toast.makeText(application, "Please input Second Road Segment distance!", Toast.LENGTH_SHORT).show()
            return
        }
        val secondRoadSegmentInt = secondRoadSegment.toString().toInt()

        if (firstRoadSegmentStatus == -1) {
            Toast.makeText(application, "Please select the first road segment status!", Toast.LENGTH_SHORT).show()
            return
        }

        if (secondRoadSegmentStatus == -1) {
            Toast.makeText(application, "Please select the second road segment status!", Toast.LENGTH_SHORT).show()
            return
        }

        if (App.instance.getBleDevice() == null) {
            BleLogger.e("App.instance.getBleDevice() == null")
            return
        }

        if (directionInt < 0) directionInt = 0
        if (directionInt > 76) directionInt = 76

        App.instance.writeData(App.instance.getBleDevice()!!,
            BleCommand.updateNavigation(directionInt.toByte(), totalTimeInt.toShort(),
                totalDistanceInt, remainingDistanceInt, currentSpeedInt.toByte(), remainingDistanceOfCurrentRoadSegmentInt,
                BleCommand.setDirectionInfo(roadSegmentItems[firstRoadSegmentStatus], firstRoadSegmentInt, roadSegmentItems[secondRoadSegmentStatus], secondRoadSegmentInt)))

    }

    private val spinnerRoadSegment = arrayOf("Road Segment Unknown", "Road Segment Clear", "Road Segment Slow", "Road Segment Blocked", "Road Segment Seriously Blocked")
    private val roadSegmentItems = arrayOf(BleCommand.ROAD_SEGMENT_UNKNOWN, BleCommand.ROAD_SEGMENT_CLEAR, BleCommand.ROAD_SEGMENT_SLOW, BleCommand.ROAD_SEGMENT_BLOCKED, BleCommand.ROAD_SEGMENT_SERIOUSLY_BLOCKED)

    var firstRoadSegmentStatus = -1
    var secondRoadSegmentStatus = -1
    private fun initFirstRoadSegmentSpinner() {
        val spinnerAdapter = ArrayAdapter(App.instance, android.R.layout.simple_spinner_item, spinnerRoadSegment)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spinnerFirstRoadSegment.adapter = spinnerAdapter
        viewBinding.spinnerFirstRoadSegment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                firstRoadSegmentStatus = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

        }
    }

    private fun initSecondRoadSegmentSpinner() {
        val spinnerAdapter = ArrayAdapter(App.instance, android.R.layout.simple_spinner_item, spinnerRoadSegment)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spinnerSecondRoadSegment.adapter = spinnerAdapter
        viewBinding.spinnerSecondRoadSegment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                secondRoadSegmentStatus = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

        }
    }

}