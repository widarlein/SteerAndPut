package se.geecity.android.steerandput.common.provider

import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.historicalstation.model.HistoricalStation
import java.util.Date

interface SelfServiceBicycleServiceProvider {
    fun fetchStations(success: (stations: List<Station>) -> Unit, failure: (error: String, throwable: Throwable?) -> Unit)
    fun fetchStationHistory(stationId: Int, from: Date, to: Date, success: (List<HistoricalStation>) -> Unit,
                            failure: (error: String, throwable: Throwable?) -> Unit)
}