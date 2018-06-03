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
package se.geecity.android.steerandput.stationlist.favorite

import android.view.View
import android.view.ViewGroup
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.stationlist.adapter.StationAdapter

/**
 * Provides logic for showing/hiding an empty view based on the changes
 * of a {@link se.geecity.android.steerandput.stationslist.adapter.StationAdapter}
 * as observed by the adapterDataObserver
 */
class EmptyView(view: View) {
    val mView = view.also {
                it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                it.visibility = View.GONE
            }

    val adapterDataObserver = object : StationAdapter.AdapterDataObserver {
        override fun onStations(stations: List<Station>) {
            when (stations.size) {
                0 -> mView.visibility = View.VISIBLE
                else -> mView.visibility = View.GONE
            }
        }
    }
}