package hu.aut.android.dm01_v11.ui.activities.deviceActivities

import hu.aut.android.dm01_v11.R

enum class ProgressCommandType (val commandType: Int, var color : Int, var message : Int){
    HIDE(0,0,0),
    CONNECT (1, R.color.progressSuccces,R.string.connecting_succes),
    DISCONNECT (2, R.color.progressing,R.string.disconnect),
    SERVERERROR (3, R.color.progressFailed,R.string.server_not_available_try_res),
    RECONNECT (4, R.color.progressing,R.string.connecting_reconnect),
    NORDICACTIVE (5, R.color.progressSuccces,R.string.company_name),
}
