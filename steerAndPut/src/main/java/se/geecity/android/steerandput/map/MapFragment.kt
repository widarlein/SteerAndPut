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
package se.geecity.android.steerandput.map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.constants.ACTION_FINISHED_REFRESHING_STATIONS
import se.geecity.android.steerandput.common.constants.EXTRA_BROADCAST_STATIONS
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.util.hasFineLocationPermission
import se.geecity.android.steerandput.common.view.StationShowingFragment

/**
 * Fragment for the map view, showing stations on a map
 */
class MapFragment : StationShowingFragment() {

    override var stations: List<Station> = mutableListOf()
    private lateinit var map: GoogleMap
    private var location: Location? = null
    private var focusStation: Station? = null

    companion object {
        fun createArgumentsBundle(stations: List<Station>, location: Location?, focusStation: Station? = null): Bundle {
            return createArguments(stations, location, focusStation)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        parseArguments()
        initMap()
    }

    fun parseArguments() {
        arguments?.also {  args ->
            parseStationsArgument(args)?.also { stations = it }
            focusStation = parseFocusStationArgument(args)
            location = parseLocationArgument(args)
        }
    }

    override fun onStart() {
        super.onStart()
        setupLocalBroadcastManager()
    }

    private fun initMap() {
        mapView.getMapAsync {
            map = it
            setupMap()
        }
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
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(mBroadcastReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mapView != null) {
            mapView.onSaveInstanceState(outState)
        }
    }

    private fun setupMap() {
        if (hasFineLocationPermission(activity!!)) {
            map.isMyLocationEnabled = true
        }

        MapsInitializer.initialize(activity)

        var zoomLevel = 15.5f
        val focusStation = focusStation
        val location = location
        val position = if (focusStation != null) {
            LatLng(focusStation.lat, focusStation.longitude)
        } else if (location != null) {
            LatLng(location.latitude, location.longitude)
        } else {
            zoomLevel = 13f
            LatLng(57.704728, 11.969011)
        }

        val cameraPosition = CameraPosition.Builder()
                .target(position)
                .zoom(zoomLevel)
                .build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        map.moveCamera(cameraUpdate)
        drawMarkers()
    }

    fun drawMarkers() {
        map.clear()
        val bikes = resources.getString(R.string.map_marker_snippet_bikes)
        val stands = resources.getString(R.string.map_marker_snippet_stands)
        val focusStations = focusStation
        stations.forEach {
            val position = LatLng(it.lat, it.longitude)
            val options = MarkerOptions()
                    .position(position)
                    .title(it.name)
                    .snippet("$bikes${it.availableBikes} $stands${it.availableBikeStands}")
            val marker = map.addMarker(options)
            if (focusStations != null && focusStations.id == it.id) {
                marker.showInfoWindow()
            }
        }
    }

    fun setupLocalBroadcastManager() {
        val intentFilter = IntentFilter(ACTION_FINISHED_REFRESHING_STATIONS)
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(mBroadcastReceiver, intentFilter)
    }

    val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val stations = intent.getParcelableArrayListExtra<Station>(EXTRA_BROADCAST_STATIONS)
            this@MapFragment.stations = stations
        }
    }
}