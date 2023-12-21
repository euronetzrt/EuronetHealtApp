package hu.euronetrt.okoskp.euronethealth.bluetooth.objects

object NordicObject {

    var counterSumCheckECG = 0
    var counterSumCheckIBI = 0
    var counterSumCheckOTHER = 0
    var counterSumCheckPPG = 0
    var counterSumCheckIMU = 0
    var counterSumCheckSTEP= 0


    val ERROR_THIS_PACKAGE = 222

    //var datasToCSVECG = mutableMapOf<String, ArrayList<Long?>>()
    var datasToCSVPPG =mutableListOf<Pair<String, ArrayList<Long?>>>()
    var datasToCSVIBI = mutableListOf<Pair<String, ArrayList<Long?>>>()
    var datasToCSVOTHER = mutableListOf<Pair<String, ArrayList<Long?>>>()
    var datasToCSVIMU = mutableListOf<Pair<String, ArrayList<Long?>>>()
    var datasToCSVSTEP = mutableListOf<Pair<String, ArrayList<Long?>>>()
}