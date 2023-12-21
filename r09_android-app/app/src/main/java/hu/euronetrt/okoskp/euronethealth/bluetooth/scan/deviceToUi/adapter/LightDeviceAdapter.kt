package hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces.ConnectDeviceWithGatt
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.dataClass.LightDeviceData
import kotlinx.android.synthetic.main.row_device_data.view.*

class LightDeviceAdapter (mycontext: Context, items: List<LightDeviceData>) : RecyclerView.Adapter<LightDeviceAdapter.ViewHolder>() {

        private val items = mutableListOf<LightDeviceData>()
        //  private var pairInterface: PairInterface
        private var context: Context
        private var connectDevice: ConnectDeviceWithGatt

        init {
            this.items.addAll(items)
            this.context = mycontext
            if (/*mycontext is PairInterface &&*/ mycontext is ConnectDeviceWithGatt) {
                // pairInterface = mycontext
                connectDevice = mycontext
            } else {
                throw RuntimeException("The Activity does not implement anything interface  (PairInterface, ConnectDeviceWithGatt ) ")
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            Log.d("DeviceScan +onBVHolder", "call")
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_device_data, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvDeviceName.text = items[position].bluetooth_name
            holder.tvDeviceAddress.text = items[position].bluetooth_address

            /*
              * True if the pair is success and false if not
              * */
            /*   val result = pairInterface.pairInterface(items[position].bluetooth_address)
               pairInterface.pairInterface(items[position].bluetooth_address)

              if (result) {
                    holder.imgBLEicon.visibility = View.VISIBLE
                    holder.btnPair.visibility = View.GONE
                    holder.btnOpen.visibility = View.VISIBLE
                } else {
                    holder.imgBLEicon.visibility = View.GONE
                }
            }*/

            holder.id_connect.setOnClickListener {
                connectDevice.connectGatt(items[position].bluetooth_address)
            }
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvDeviceName: TextView = itemView.tvDeviceName
            var tvDeviceAddress: TextView = itemView.tvDeviceAddress
            var id_connect: Button = itemView.id_connect as Button
        }
}