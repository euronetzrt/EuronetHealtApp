package hu.aut.android.dm01_v11.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.ProgressCommandType
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.GlobalRes.applicationVersionModeIsLight
import hu.euronetrt.okoskp.euronethealth.GlobalRes.connectedDevice
import hu.euronetrt.okoskp.euronethealth.bluetooth.BluetoothServiceNotificationType
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataListener
import hu.euronetrt.okoskp.euronethealth.data.broadcast.IBIListener
import hu.euronetrt.okoskp.euronethealth.data.broadcast.PPGListener
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.IBIData
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.PPGData
import kotlinx.android.synthetic.main.fragment_device_nordic_page.*

@Suppress("UNCHECKED_CAST")
class FragmentDeviceNordicPage : Fragment() {

    private var bleService: BluetoothLeService? = null
    private lateinit var mView: View
    private var now = System.currentTimeMillis() + 500
    private var waitDestroy = false


    companion object {
        val TAG = "FRAG_DEV_NORDIC_PAGE"
    }

    private var ppgListener: PPGListener = object : PPGListener {
        override fun onDataArrived(data: PPGData) {
            if (!applicationVersionModeIsLight) {
                waitDestroy = true
                if (view != null) {

                    view!!.post {
                        data.getAmplitude().forEach {
                            addEntry(it.getValue())
                        }
                    }
                }
                waitDestroy = false
            }
        }
    }

    private var ibiListener: IBIListener = object : IBIListener {
        override fun onDataArrived(data: IBIData) {
            if (!applicationVersionModeIsLight) {
                waitDestroy = true
                if (System.currentTimeMillis() > now) {
                    now = System.currentTimeMillis() + 1500
                    if (view != null) {
                        view!!.post {
                            data.getIbiValue().forEach {
                                addEntryIBI(it.getValue())
                            }
                        }
                    }
                }
                waitDestroy = false
            }
        }
    }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_device_nordic_page, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // (activity as DeviceMainActivity).refreshingFalse()

            start_nordic.setOnClickListener {
                if (connectedDevice) {
                    if (bleService == null) {
                        bleService = (activity!! as DeviceMainActivity).getBleServiceReference()
                    }
                    if (bleService != null) {
                        /*null ként van inicializálva de ettől még lehet hogy az értékadásban is nullt kap mert nincs kapcsolat a szervizzel.*/
                        (activity!! as DeviceMainActivity).mainProgressController(ProgressCommandType.NORDICACTIVE.commandType, false)
                        bleService!!.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, true)
                        val handler = Handler()

                        handler.postDelayed({
                            bleService!!.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, true)
                        }, 1000)

                        if (!applicationVersionModeIsLight) {
                            DataBroadcaster.getInstance(view.context).addListener(CollectableType.PPG, ppgListener as DataListener<AbstractData>)
                            DataBroadcaster.getInstance(view.context).addListener(CollectableType.IBI, ibiListener as DataListener<AbstractData>)
                        }
                    }
                    GlobalRes.registredNordicBroadcast = true
                }
            }

            stop_nordic.setOnClickListener {
                if (!applicationVersionModeIsLight) {
                    DataBroadcaster.getInstance(view.context).removeListener(CollectableType.PPG, ppgListener as DataListener<AbstractData>)
                    DataBroadcaster.getInstance(view.context).removeListener(CollectableType.IBI, ibiListener as DataListener<AbstractData>)
                }
                if (connectedDevice) {
                    if (bleService != null) {
                        GlobalRes.registredNordicBroadcast = false
                        (activity!! as DeviceMainActivity).mainProgressController(ProgressCommandType.HIDE.commandType, false)
                        bleService!!.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)

                        val handler = Handler()
                        handler.postDelayed({
                            bleService!!.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
                        }, 1000)
                        bleService!!.writeLastDatasToCSV()
                    }
                }
            }

            initChart()
            initChartRR()
        }


        private fun initChartRR() {
            chartSensorNordicRR.description.isEnabled = true  // bekapcsoljuk a feliratot
            chartSensorNordicRR.setTouchEnabled(false) // lehessen rákattintani
            chartSensorNordicRR.isDragEnabled = false//
            chartSensorNordicRR.setScaleEnabled(false)//
            chartSensorNordicRR.setDrawGridBackground(false)
            chartSensorNordicRR.setPinchZoom(false) // lehessen belezoom-olni
            chartSensorNordicRR.setBackgroundColor(Color.argb(100, 63, 182, 216)) //milyen színsémával dolgozzon

            val data = LineData()
            data.setValueTextColor(Color.BLACK) //szöveg szín
            chartSensorNordicRR.data = data  //adat

            val l = chartSensorNordicRR.legend
            l.form = Legend.LegendForm.LINE
            l.textColor = Color.BLACK

            val xl = chartSensorNordicRR.xAxis //grid vonalak
            xl.textColor = Color.BLACK
            xl.setDrawGridLines(true)
            xl.setAvoidFirstLastClipping(true)
            xl.labelCount = 0
            xl.isEnabled = true

            val leftAxis = chartSensorNordicRR.axisLeft
            leftAxis.textColor = Color.BLACK
            leftAxis.setDrawGridLines(true)
            leftAxis.setDrawGridLines(true)
            leftAxis.setCenterAxisLabels(true)

            val rightAxis = chartSensorNordicRR.axisRight
            rightAxis.isEnabled = true

            chartSensorNordicRR.axisLeft.setDrawGridLines(true)
            chartSensorNordicRR.xAxis.setDrawGridLines(true)
            chartSensorNordicRR.setDrawBorders(true)
            chartSensorNordicRR.isAutoScaleMinMaxEnabled = true
        }

        private fun initChart() {
            chartSensorNordic.description.isEnabled = true  // bekapcsoljuk a feliratot
            chartSensorNordic.setTouchEnabled(false) // lehessen rákattintani
            chartSensorNordic.isDragEnabled = false//
            chartSensorNordic.setScaleEnabled(false)//
            chartSensorNordic.setDrawGridBackground(false)
            chartSensorNordic.setPinchZoom(false) // lehessen belezoom-olni
            chartSensorNordic.setBackgroundColor(Color.argb(100, 63, 182, 216)) //milyen színsémával dolgozzon

            val data = LineData()
            data.setValueTextColor(Color.BLACK) //szöveg szín
            chartSensorNordic.data = data  //adat

            val l = chartSensorNordic.legend
            l.form = Legend.LegendForm.LINE
            l.textColor = Color.BLACK

            val xl = chartSensorNordic.xAxis //grid vonalak
            xl.textColor = Color.BLACK
            xl.setDrawGridLines(true)
            xl.setAvoidFirstLastClipping(true)
            xl.labelCount = 0
            xl.isEnabled = true

            val leftAxis = chartSensorNordic.axisLeft
            leftAxis.textColor = Color.BLACK
            leftAxis.setDrawGridLines(true)
            leftAxis.setDrawGridLines(true)
            leftAxis.setCenterAxisLabels(true)

            val rightAxis = chartSensorNordic.axisRight
            rightAxis.isEnabled = true

            chartSensorNordic.axisLeft.setDrawGridLines(true)
            chartSensorNordic.xAxis.setDrawGridLines(true)
            chartSensorNordic.setDrawBorders(true)
            chartSensorNordic.isAutoScaleMinMaxEnabled = true
        }

        fun addEntry(data: Long) {
            if (chartSensorNordic != null) {
                if (chartSensorNordic.data != null) {

                    var set: ILineDataSet? = chartSensorNordic.data.getDataSetByIndex(0)
                    // set.addEntry(...); // can be called as well

                    if (set == null) {
                        set = createSet()
                        chartSensorNordic.data.addDataSet(set)
                    }

                    chartSensorNordic.data.addEntry(Entry(set.entryCount.toFloat(), data.toFloat()
                    ), 0)
                    chartSensorNordic.data.notifyDataChanged()

                    chartSensorNordic.notifyDataSetChanged()

                    // limit the number of visible entries
                    chartSensorNordic.setVisibleXRangeMaximum(900F)
                    //chartSensor.setVisibleYRange(30, AxisDependency.LEFT);

                    // move to the latest entry
                    chartSensorNordic.moveViewToX(chartSensorNordic.data.entryCount.toFloat())
                }
            }
        }

        fun addEntryIBI(data: Long) {
            if (chartSensorNordicRR != null) {


                if (chartSensorNordicRR.data != null) {

                    var set: ILineDataSet? = chartSensorNordicRR.data.getDataSetByIndex(0)
                    // set.addEntry(...); // can be called as well

                    if (set == null) {
                        set = createSetRR()
                        chartSensorNordicRR.data.addDataSet(set)
                    }

                    chartSensorNordicRR.data.addEntry(Entry(set.entryCount.toFloat(), data.toFloat()
                    ), 0)
                    chartSensorNordicRR.data.notifyDataChanged()

                    chartSensorNordicRR.notifyDataSetChanged()

                    // limit the number of visible entries
                    chartSensorNordicRR.setVisibleXRangeMaximum(900F)
                    //chartSensor.setVisibleYRange(30, AxisDependency.LEFT);

                    // move to the latest entry
                    chartSensorNordicRR.moveViewToX(chartSensorNordicRR.data.entryCount.toFloat())
                }
            }
        }

        private fun createSet(): LineDataSet {
            val set = LineDataSet(null, "Sensor Data")
            set.axisDependency = YAxis.AxisDependency.LEFT
            set.lineWidth = 3f
            set.color = Color.BLACK
            set.isHighlightEnabled = false
            set.setDrawValues(false)
            set.setDrawCircles(false)
            set.mode = LineDataSet.Mode.CUBIC_BEZIER
            set.cubicIntensity = 0.2f
            return set
        }

        private fun createSetRR(): LineDataSet {
            val set = LineDataSet(null, "Sensor Data")
            set.axisDependency = YAxis.AxisDependency.LEFT
            set.lineWidth = 3f
            set.color = Color.BLACK
            set.isHighlightEnabled = false
            set.setDrawValues(false)
            set.setDrawCircles(false)
            set.mode = LineDataSet.Mode.CUBIC_BEZIER
            set.cubicIntensity = 0.2f
            return set
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Log.d(TAG, "onCreate nordic fragment")
        }

        override fun onStart() {
            super.onStart()
            Log.d(TAG, "onStart nordic fragment")
        }

        override fun onResume() {
            super.onResume()
        }

        override fun onPause() {
            super.onPause()
        }

        override fun onStop() {
            super.onStop()
            Log.d(TAG, "onStop nordic fragment")
        }

        override fun onDestroy() {
            Log.d(TAG, "onDestroy nordic fragment")
            val lock = Object()
            while (waitDestroy) {
                synchronized(lock) {
                    lock.wait()
                }
            }
            synchronized(lock) {
                lock.notify()
            }
            super.onDestroy()

        }

        override fun onDestroyView() {
            super.onDestroyView()
        }
    }