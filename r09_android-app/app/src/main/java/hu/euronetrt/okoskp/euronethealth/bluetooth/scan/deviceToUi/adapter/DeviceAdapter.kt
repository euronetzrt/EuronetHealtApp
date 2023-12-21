package hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces.ConnectDeviceWithGatt
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.dataClass.DeviceData
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.MultipleDeviceInfo
import kotlinx.android.synthetic.main.row_device_data.view.*

class DeviceAdapter(myContext: Context, items: List<DeviceData>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    private val items = mutableListOf<DeviceData>()
    private var context: Context
    private var connectDevice: ConnectDeviceWithGatt

    init {
        this.items.addAll(items)
        this.context = myContext
        if (myContext is ConnectDeviceWithGatt) {
            connectDevice = myContext
        } else {
            throw RuntimeException("The Activity does not implement anything interface (PairInterface, ConnectDeviceWithGatt ) ")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_device_data, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvDeviceName.text = items[position].bluetooth_name
        holder.tvDeviceManufacturer.text = items[position].bluetooth_manufacturer
        holder.tvDeviceType.text = items[position].bluetooth_type.toString()
        holder.tvDeviceAddress.text = items[position].bluetooth_address
        holder.deviceObject = items[position].bluetooth_device_Object!!

        holder.idConnect.setOnClickListener {
            connectDevice.connectGatt(items[position].bluetooth_device_Object!!)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDeviceName: TextView = itemView.tvDeviceName
        var tvDeviceManufacturer: TextView = itemView.tvDeviceManufacturer
        var tvDeviceType: TextView = itemView.tvDeviceType
        var tvDeviceAddress: TextView = itemView.tvDeviceAddress
        var idConnect: Button = itemView.id_connect as Button
        lateinit var deviceObject : Array<MultipleDeviceInfo>
    }
}