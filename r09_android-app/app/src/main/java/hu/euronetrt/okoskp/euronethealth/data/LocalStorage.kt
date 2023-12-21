package hu.euronetrt.okoskp.euronethealth.data

import android.content.Context
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.data.broadcast.*
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.*

@Suppress("UNCHECKED_CAST")
class LocalStorage {

    private var mContext: Context

    constructor(mContext: Context) {
        this.mContext = mContext
        start()
    }

    /**
     * ppgListener implementation
     */
    private var ppgListener: PPGListener = object : PPGListener {
        override fun onDataArrived(data: PPGData) {
            this@LocalStorage.onDataArrived(data)
        }
    }

    private var ibiListener: IBIListener = object : IBIListener {
        override fun onDataArrived(data: IBIData) {
            this@LocalStorage.onDataArrived(data)
        }
    }

    private var imuListener: IMUListener = object : IMUListener {
        override fun onDataArrived(data: IMUData) {
            this@LocalStorage.onDataArrived(data)
        }
    }


    private var hrListener: HRListener = object : HRListener {
        override fun onDataArrived(data: HRData) {
            this@LocalStorage.onDataArrived(data)
        }
    }

    private var otherListener : OTHERListener = object : OTHERListener{
        override fun onDataArrived(data: OTHERData) {
            this@LocalStorage.onDataArrived(data)
        }
    }

    private var stepListener : StepListener = object : StepListener{
        override fun onDataArrived(data: StepData) {
            this@LocalStorage.onDataArrived(data)
        }
    }

    companion object {

        private lateinit var INSTANCE: LocalStorage
        private val TAGDATAPROC = "AbstractData"
        private val TAG = "TAGDATAPROC"

        @Synchronized
        fun getInstance(mContext: Context): LocalStorage {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = LocalStorage(mContext)
            }
            return INSTANCE
        }
    }

    fun start() {
        DataBroadcaster.getInstance(mContext).addListener(CollectableType.PPG, ppgListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).addListener(CollectableType.IBI, ibiListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).addListener(CollectableType.HEARTRATE, hrListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).addListener(CollectableType.IMU, imuListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).addListener(CollectableType.OTHER, otherListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).addListener(CollectableType.STEP, stepListener as DataListener<AbstractData>)
    }

    fun stop() {
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.PPG, ppgListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.IBI, ibiListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.HEARTRATE, hrListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.IMU, imuListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.OTHER, otherListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.STEP, stepListener as DataListener<AbstractData>)
    }

    fun onDataArrived(data: IBIData) {
      //  val dbThread = Thread {
            val id = AppDatabase.getInstance(mContext).ibiDatabaseDao().insertItem(data)
            Log.d(TAGDATAPROC, "class: $TAG  --> IBIData database insert done and id this $id")

            data.getIbiValue().forEach {
                it.setIbiDataId(id)
            }

            AppDatabase.getInstance(mContext)
                    .ibiValueDatabaseDao()
                    .insertAll(data.getIbiValue())
       // }
       // dbThread.start()
    }

    /**
     * ppgListener's onDataArrived fun
     *
     * @param data
     */
    fun onDataArrived(data: PPGData) {
       // val dbThread = Thread {

            val id = AppDatabase.getInstance(mContext).ppgDatabaseDao().insertItem(data)

            Log.d(TAGDATAPROC, "class: $TAG  --> PPGData database insert done and id this $id")

            data.getAmplitude().forEach {
                it.setPpgDataId(id)
            }

            AppDatabase.getInstance(mContext)
                    .ppgValueDatabaseDao()
                    .insertAll(
                            data.getAmplitude()
                    )

            Log.d(TAGDATAPROC, "class: $TAG  --> PPGData database insert done and id this $id")
      //  }
      //  dbThread.start()
    }

    fun onDataArrived(data: IMUData) {

       // val dbThread = Thread {
            val id = AppDatabase.getInstance(mContext).imuDatabaseDao().insertItem(data)
            Log.d(TAGDATAPROC, "class: $TAG  --> IMUData database insert done and id this $id")

            data.getImuValue().forEach {
                it.setImuDataId(id)
            }

            AppDatabase.getInstance(mContext)
                    .imuValueDatabaseDao()
                    .insertAll(data.getImuValue())
      //  }
      //  dbThread.start()
    }

    fun onDataArrived(data: HRData) {
        //    val dbThread = Thread
       // {
            val id = AppDatabase.getInstance(mContext).hrDatabaseDao().insertItem(data)
            Log.d(TAGDATAPROC, "class: $TAG  --> HRData database insert done and id this $id")
       // }
       // dbThread.start()

    }

   fun onDataArrived(data: OTHERData) {
      //  val dbThread = Thread {
            val id = AppDatabase.getInstance(mContext).otherDatabaseDao().insertItem(data)
            Log.d(TAGDATAPROC, "class: $TAG  --> OtherData database insert done and id this $id")
      //  }
      //  dbThread.start()
    }

    fun onDataArrived(data: StepData) {
    //    val dbThread = Thread {
            val id = AppDatabase.getInstance(mContext).stepDatabaseDao().insertItem(data)
            Log.d(TAGDATAPROC, "class: $TAG  --> StepData database insert done and id this $id")
      //  }
      //  dbThread.start()
    }
}