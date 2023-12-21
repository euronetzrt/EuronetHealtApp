package hu.aut.android.dm01_v11.ui.activities.deviceActivities

import hu.aut.android.dm01_v11.R

enum class ScanAndPairProgressCommandType (val commandType: Int, var message : Int){
    HIDE(0,0),
    GATTCONNECTING (1,R.string.connecting),
    GATTCONNECTED (2,R.string.connecting_succes),
    SCANSTART (3,R.string.connecting_succes),
    SCANSTOP (4,R.string.connecting_succes),
    SERVERCONNECT (5, R.string.pairWait),
    SERVERNOTAVAILABLE(6,R.string.serverNot_available),
    SERVERNOTCONNECTED (7,R.string.server_not_available_try_res),
    BLUETOOTHDISABLED (8,R.string.bluetoothDesabled)
}
