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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_station.*
import org.koin.android.ext.android.inject
import se.geecity.android.steerandput.NavigationManager
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.constants.ACTION_BROADCAST_NEW_LOCATION
import se.geecity.android.steerandput.common.constants.ACTION_FINISHED_REFRESHING_STATIONS
import se.geecity.android.steerandput.common.constants.EXTRA_BROADCAST_LOCATION
import se.geecity.android.steerandput.common.constants.EXTRA_BROADCAST_STATIONS
import se.geecity.android.steerandput.common.logging.FirebaseLogger
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.util.getDistanceBetweenAsString
import se.geecity.android.steerandput.common.util.hasCoarseLocationPermission
import se.geecity.android.steerandput.common.util.hasFineLocationPermission
import se.geecity.android.steerandput.common.view.StationShowingFragment
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.historicalstation.model.HistoricalStation
import se.geecity.android.steerandput.map.MapFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StationFragment : StationShowingFragment(), StationView {

    override var stations: List<Station> = mutableListOf()
        set(value) {
            value.find { it.id == station.id }?.let {
                station = it
                updateStationInfo(it)
            }
            field = value
        }

    private val stationPresenter: StationPresenter by inject()

    private lateinit var station: Station
    private var location: Location? = null

    private val xAxisValueFormatter = object : IAxisValueFormatter {
        private val dateFormatter = SimpleDateFormat("HH:mm", Locale.US)
        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
            return dateFormatter.format(Date(value.toLong()))
        }
    }

    private val firebaseLogger: FirebaseLogger by inject()

    companion object {
        private val ARG_STATION = "station"
        private val ARG_LOCATION = "location"

        fun createArgumentBundle(stations: List<Station>, station: Station, location: Location?): Bundle {
            val args = createArguments(stations, location)
            args.putParcelable(ARG_STATION, station)
            return args
        }

        private fun parseStation(arguments: Bundle): Station = arguments.getParcelable(ARG_STATION)
        private fun parseLocation(arguments: Bundle): Location? = arguments.getParcelable(ARG_LOCATION)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_station, container, false)

        stationPresenter.stationView = this
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mapView.onCreate(savedInstanceState)

        parseArguments()

        val location = location
        if (location != null) {
            setLocationText(location)
        }
        setupChart()
        setupMap()

        stationPresenter.stationId = station.id
        stationPresenter.onViewCreated()
        firebaseLogger.pageView(ViewIdentifier.STATION, station)
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_BROADCAST_NEW_LOCATION)
        intentFilter.addAction(ACTION_FINISHED_REFRESHING_STATIONS)
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(broadcastReceiver)
    }

    private fun parseArguments() {
        arguments?.also { args ->
            station = parseStation(args)
            location = parseLocation(args)
            stations = checkNotNull(parseStationsArgument(args))
        }
    }

    override fun onStationHistory(history: List<HistoricalStation>) {
        val activity = activity ?: return
        val dataEntries = history.map { Entry(it.viewDate.time.toFloat(), it.availableBikes.toFloat()) }

        val lineDataSet = LineDataSet(dataEntries, "Available Bikes").apply {
            lineWidth = 3f
            setDrawValues(false)
        }
        val lineData = LineData(lineDataSet)
        chart.data = lineData

        activity.runOnUiThread { chart.invalidate() }
    }

    override fun navigateToMap(station: Station) {
        val request = NavigationManager.NavigationRequest(ViewIdentifier.MAP, MapFragment.createArgumentsBundle(stations, location, station))
        NavigationManager.instance?.navigate(request)
    }

    fun setLocationText(location: Location) {
        stationDistance.text = getDistanceBetweenAsString(station, location)
    }

    private fun setupChart() {
        chart.axisLeft.granularity = 1f
        chart.axisRight.isEnabled = false
        chart.axisLeft.setDrawGridLines(false)
        chart.legend.isEnabled = false
        chart.xAxis.valueFormatter = xAxisValueFormatter
        chart.xAxis.granularity = 800000f
        chart.xAxis.setDrawGridLines(false)
        chart.description.isEnabled = false
    }

    private fun updateStationInfo(station: Station) {
        stationName.text = station.name
        stationBikeText.text = station.availableBikes.toString()
        stationStandsText.text = station.availableBikeStands.toString()
    }

    private fun setupMap() {
        mapView.getMapAsync { map ->
            if (hasFineLocationPermission(activity!!) || hasCoarseLocationPermission(activity!!)) {
                map.isMyLocationEnabled = true
            }

            val pos = LatLng(station.lat, station.longitude)
            val cp = CameraPosition.Builder()
                    .target(pos)
                    .zoom(15.5f)
                    .build()
            val update = CameraUpdateFactory.newCameraPosition(cp)
            map.moveCamera(update)

            map.uiSettings.isMapToolbarEnabled = false

            val markerOptions = MarkerOptions().position(pos)
            map.addMarker(markerOptions)

            map.setOnMapClickListener {
                stationPresenter.mapClick(station)
                firebaseLogger.mapClicked(station)
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_BROADCAST_NEW_LOCATION -> setLocationText(intent.getParcelableExtra(EXTRA_BROADCAST_LOCATION))
                ACTION_FINISHED_REFRESHING_STATIONS -> stations = intent.getParcelableArrayListExtra(EXTRA_BROADCAST_STATIONS)
            }
        }
    }
}