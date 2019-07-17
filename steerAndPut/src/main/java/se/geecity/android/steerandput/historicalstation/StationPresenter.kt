/*
 * MIT License
 *
 * Copyright (c) 2019 Alexander Widar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package se.geecity.android.steerandput.historicalstation

import android.util.Log
import se.geecity.android.steerandput.Result
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.historicalstation.interactor.StationInteractor
import se.geecity.android.steerandput.historicalstation.model.HistoricalStation
import java.util.Calendar
import java.util.Date
import kotlin.properties.Delegates

class StationPresenter(private val stationInteractor: StationInteractor) {

    val periodHours = DEFAULT_PERIOD_BACK_IN_TIME_HOURS
    var stationId: Int by Delegates.notNull()

    lateinit var stationView: StationView

    companion object {
        private const val DEFAULT_PERIOD_BACK_IN_TIME_HOURS = 6
    }

    private val stationCallback: (Result<List<HistoricalStation>>) -> Unit = { result ->
        if (result.success) {
            val historicalStations = result.resultValue.takeLastWhile {
                Date().time - it.viewDate.time < periodHours.hoursToMillis()
            }

            stationView.onStationHistory(historicalStations)
        }
    }

    fun onViewCreated() {

        val (to, from) = getToAndFromDates()
        stationInteractor.getStationHistory(stationId, to, from, stationCallback)
    }

    fun mapClick(station: Station) {
        stationView.navigateToMap(station)
    }

    private fun getToAndFromDates(): Pair<Date, Date> {
        val c = Calendar.getInstance()
        c.add(Calendar.DATE, 1)
        val to = c.time
        c.add(Calendar.DAY_OF_MONTH, -2)
        val from = c.time
        return Pair(to, from)
    }
}

private fun Int.hoursToMillis(): Int {
    return this * 60 * 60 * 1000
}