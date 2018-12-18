/*
 * MIT License
 * 
 * Copyright (c) 2018 Alexander Widar
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
package se.geecity.android.steerandput.stationlist.list

import android.location.Location
import android.os.Bundle
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.view.StationShowingFragment
import se.geecity.android.steerandput.stationlist.AbsListFragment

class ListFragment : AbsListFragment() {

    override var stations: List<Station> = mutableListOf()
    set(value) {
        field = value
        updateList()
    }

    companion object {
        fun createArgumentsBundle(stations: List<Station>?, location: Location?): Bundle =
                StationShowingFragment.createArguments(stations, location)

        fun newFragment(stations: List<Station>?, location: Location?): ListFragment =
                ListFragment().apply { arguments = createArgumentsBundle(stations, location) }
    }

    override fun onStart() {
        super.onStart()
        if (stations.isEmpty()) {
            requestStations()
        }
    }

    override fun parseArguments() {
        super.parseArguments()
        val stations = StationShowingFragment.parseStationsArgument(arguments!!)
        if (stations != null) {
            this.stations = stations
        } else if (!this.stations.isEmpty()) {
            updateList()
        }
    }

    override fun initialize() {}

    override fun updateList() {
        adapter.favorites = favoriteUtil.favorites
        adapter.stations = stations
    }
}