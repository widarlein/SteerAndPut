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
package se.geecity.android.steerandput.common.view

import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import se.geecity.android.steerandput.common.model.Station
import java.util.ArrayList

abstract class StationShowingFragment : Fragment() {

    companion object {
        const val ARG_STATIONS = "stations"
        const val ARG_LOCATION = "location"
        const val ARG_FOCUS_STATION = "focus_station"

        fun createArguments(stations: List<Station>?, location: Location?, focusStation: Station? = null): Bundle {
            val bundle = Bundle()
            if (stations != null) {
                bundle.putParcelableArrayList(ARG_STATIONS, stations as ArrayList<out Parcelable>)
            }

            if (location != null) {
                bundle.putParcelable(ARG_LOCATION, location)
            }

            if (focusStation != null) {
                bundle.putParcelable(ARG_FOCUS_STATION, focusStation)
            }

            return bundle
        }

        fun parseStationsArgument(args: Bundle): List<Station>? {
            return args.getParcelableArrayList(ARG_STATIONS)
        }

        fun parseFocusStationArgument(args: Bundle): Station? = args.getParcelable(ARG_FOCUS_STATION)

        fun parseLocationArgument(args: Bundle): Location? = args.getParcelable(ARG_LOCATION)
    }

    abstract var stations: List<Station>
}