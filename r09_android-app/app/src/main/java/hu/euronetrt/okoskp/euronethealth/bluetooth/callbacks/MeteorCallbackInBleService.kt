package hu.euronetrt.okoskp.euronethealth.bluetooth.callbacks

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.GlobalRes.SERVER_CONN
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.TAGSCAN
import im.delight.android.ddp.MeteorCallback

@SuppressLint("StaticFieldLeak")
object MeteorCallbackInBleService : MeteorCallback {

    private val TAG = "BlMeScCb"
    private lateinit var contextBLE : Context

    fun setBleContect(context: Context) {
        contextBLE = context
    }

    override fun onConnect(signedInAutomatically: Boolean) {
        if (signedInAutomatically) {
            // bejelentkeztünk az adatainkkal.
            Log.d(TAGSCAN + TAG, "signedInAutomatically -----> true" )
            val serverConnected = Intent()
            serverConnected.action = SERVER_CONN
            contextBLE.sendBroadcast(serverConnected)
        }else{
            Log.d(TAGSCAN + TAG, "signedInAutomatically -----> false" )
        }
    }

    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
        val mCollectionName = "onDataAdded $collectionName"
        val mDocumentID = "onDataAdded $documentID"
        val mNewValuesJson = "onDataAdded$newValuesJson"

        Log.d(TAGSCAN + TAG, mCollectionName)
        Log.d(TAGSCAN + TAG, mDocumentID)
        Log.d(TAGSCAN + TAG, mNewValuesJson)
    }

    override fun onDataRemoved(collectionName: String?, documentID: String?) {
        val mCollectionName = "onDataRemoved $collectionName"
        val mDocumentID = "onDataRemoved $documentID"

        Log.d(TAGSCAN + TAG, mCollectionName)
        Log.d(TAGSCAN + TAG, mDocumentID)
    }

    override fun onException(e: Exception?) {
        val error = "onError error" + e!!.message
        Log.d(TAGSCAN + TAG, error)
    }

    override fun onDisconnect() {
        val ondisconnect = "onDisconnect"
        Log.d(TAGSCAN + TAG, ondisconnect)
    }

    override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
        val mCollectionName = "onDataChanged $collectionName"
        val mDocumentID = "onDataChanged $documentID"
        val mNewValuesJson = "onDataChanged $updatedValuesJson"
        val mRemovedValuesJson = "onDataChanged $removedValuesJson"

        Log.d(TAGSCAN + TAG, mCollectionName)
        Log.d(TAGSCAN + TAG, mDocumentID)
        Log.d(TAGSCAN + TAG, mNewValuesJson)
        Log.d(TAGSCAN + TAG, mRemovedValuesJson)
    }
}