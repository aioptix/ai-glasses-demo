package com.xflip.arglassesdemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bhm.ble.BleManager
import com.bhm.ble.device.BleDevice
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.xflip.arglassesdemo.R
import com.xflip.arglassesdemo.databinding.LayoutRecyclerItemBinding

class DeviceListAdapter(data: MutableList<BleDevice>?
) : BaseQuickAdapter<BleDevice, DeviceListAdapter.VH>(0, data) {

    class VH(
        parent: ViewGroup,
        val binding: LayoutRecyclerItemBinding = LayoutRecyclerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : BaseViewHolder(binding.root)

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun convert(holder: VH, item: BleDevice) {
        holder.binding.tvName.text = buildString {
            append(item.deviceName)
            append(", ")
            append(item.deviceAddress)
        }
        val rssi = item.rssi ?: 0
        when {
            rssi >= -65 -> {
                holder.binding.ivRssi.setImageResource(R.drawable.baseline_signal_cellular_alt_24)
            }
            rssi >= -85 -> {
                holder.binding.ivRssi.setImageResource(R.drawable.baseline_signal_cellular_alt_2_bar_24)
            }
            else -> {
                holder.binding.ivRssi.setImageResource(R.drawable.baseline_signal_cellular_alt_1_bar_24)
            }
        }
        if (BleManager.get().isConnected(item)) {
            holder.binding.btnConnect.text = "Disconnect"
            holder.binding.btnOperate.isEnabled = true
            holder.binding.btnConnect.setBackgroundColor(
                ContextCompat
                    .getColor(holder.binding.btnConnect.context, R.color.red))
        } else {
            holder.binding.btnConnect.text = "Connect"
            holder.binding.btnOperate.isEnabled = false
            holder.binding.btnConnect.setBackgroundColor(
                ContextCompat
                .getColor(holder.binding.btnConnect.context, R.color.purple_200))
        }
    }

}