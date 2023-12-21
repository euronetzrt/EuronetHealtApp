package hu.euronetrt.okoskp.euronethealth.dfuManager

import android.util.Log
import im.delight.android.ddp.MeteorCallback

class DFUMeteorCallBack : MeteorCallback {

    companion object{
        private val TAG = "DFUMeteorCallBack"
    }
    override fun onConnect(signedInAutomatically: Boolean) {
        if (signedInAutomatically) {
            // bejelentkeztünk az adatainkkal.
            Log.d(TAG, "DFUManager signedInAutomatically -----> true" )
        }else{
            Log.d(TAG, "DFUManager signedInAutomatically -----> false" )
        }
    }

    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
        val mCollectionName = "DFUManager onDataAdded $collectionName"
        val mDocumentID = "DFUManager onDataAdded $documentID"
        val mNewValuesJson = "DFUManager onDataAdded$newValuesJson"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
        Log.d(TAG, mNewValuesJson)
    }

    override fun onDataRemoved(collectionName: String?, documentID: String?) {
        val mCollectionName = "DFUManager onDataRemoved $collectionName"
        val mDocumentID = "DFUManager onDataRemoved $documentID"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
    }

    override fun onException(e: Exception?) {
        val error = "DFUManager onError error" + e!!.message
        Log.d(TAG, error)
    }

    override fun onDisconnect() {
        val ondisconnect = "DFUManager onDisconnect"
        Log.d(TAG, ondisconnect)
    }

    override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
        val mCollectionName = "DFUManager onDataChanged $collectionName"
        val mDocumentID = "DFUManager onDataChanged $documentID"
        val mNewValuesJson = "DFUManager onDataChanged $updatedValuesJson"
        val mRemovedValuesJson = "DFUManager onDataChanged $removedValuesJson"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
        Log.d(TAG, mNewValuesJson)
        Log.d(TAG, mRemovedValuesJson)
    }
}