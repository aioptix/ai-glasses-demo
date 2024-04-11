package com.xflip.arglassesdemo.ui

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhm.ble.device.BleDevice
import com.xflip.arglassesdemo.R
import com.xflip.arglassesdemo.adapter.DeviceListAdapter
import com.xflip.arglassesdemo.base.BaseActivity
import com.xflip.arglassesdemo.databinding.ActivityMainBinding
import com.xflip.arglassesdemo.entity.MessageEvent
import com.xflip.arglassesdemo.utils.ViewUtil
import com.xflip.arglassesdemo.vm.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    private var listAdapter: DeviceListAdapter? = null

    override fun initEvent() {
        super.initEvent()
    }

    override fun createViewModel() = MainViewModel(application)

    override fun initData() {
        super.initData()
        initList()
        initListener()
        viewModel.initBle()
    }

    override fun onMessageEvent(event: MessageEvent?) {
        super.onMessageEvent(event)
    }

    private fun initList() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        viewBinding.recyclerView.setHasFixedSize(true)
        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        // Solve the problem of flickering when RecyclerView is partially refreshed
        (viewBinding.recyclerView.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        listAdapter = DeviceListAdapter(viewModel.listDRData)
        viewBinding.recyclerView.adapter = listAdapter
    }

    private fun initListener() {
        lifecycleScope.launch {
            // Add device and refresh list
            viewModel.listDRStateFlow.collect{
                if (it.deviceName != null && it.deviceAddress != null) {
                    val position = (listAdapter?.itemCount?: 1) - 1
                    listAdapter?.notifyItemInserted(position)
                    viewBinding.recyclerView.smoothScrollToPosition(position)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.scanStopStateFlow.collect {
                viewBinding.btnStart.text = if (it) {"Start Scan"} else {"Scanning"}
                viewBinding.btnStart.isEnabled = it
                viewBinding.btnStop.isEnabled = !it
            }
        }

        lifecycleScope.launch {
            // Refresh list after connected device
            viewModel.refreshStateFlow.collect {
                delay(300)
                if (it?.bleDevice == null) {
                    listAdapter?.notifyDataSetChanged()
                    return@collect
                }
                it.bleDevice.let { bleDevice ->
                    val position = listAdapter?.data?.indexOf(bleDevice) ?: -1
                    if (position >= 0) {
                        listAdapter?.notifyItemChanged(position)
                    }
                }
            }
        }

        listAdapter?.addChildClickViewIds(R.id.btnConnect, R.id.btnOperate)
        listAdapter?.setOnItemChildClickListener { adapter, view, position ->
            if (ViewUtil.isInvalidClick(view)) {
                return@setOnItemChildClickListener
            }
            val bleDevice: BleDevice? = adapter.data[position] as BleDevice?
            if (view.id == R.id.btnConnect) {
                if (viewModel.isConnected(bleDevice)) {
                    viewModel.disConnect(bleDevice)
                } else {
                    viewModel.connect(bleDevice)
                }
            } else if (view.id == R.id.btnOperate) {
                startActivity(Intent(this@MainActivity, ControlActivity::class.java))
            }
        }

        viewBinding.btnStart.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            listAdapter?.notifyItemRangeRemoved(0, viewModel.listDRData.size)
            viewModel.listDRData.clear()
            viewModel.startScan(this@MainActivity)
        }

        viewBinding.btnStop.setOnClickListener {
            if (ViewUtil.isInvalidClick(it)) {
                return@setOnClickListener
            }
            viewModel.stopScan()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopScan()
        viewModel.close()
    }

}