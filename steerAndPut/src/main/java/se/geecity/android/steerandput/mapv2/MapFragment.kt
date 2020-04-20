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
package se.geecity.android.steerandput.mapv2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import se.geecity.android.data.model.ParcelableStationObject
import se.geecity.android.data.model.toParcelable
import se.geecity.android.domain.entities.Failure
import se.geecity.android.domain.entities.Resource
import se.geecity.android.domain.entities.StationObject
import se.geecity.android.domain.entities.Success
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.logging.FirebaseLoggerV2
import se.geecity.android.steerandput.common.util.hasFineLocationPermission
import se.geecity.android.steerandput.common.view.ViewIdentifier

class MapFragment : Fragment() {

    private val mapViewModel: MapViewModel by viewModel()
    private val firebaseLogger: FirebaseLoggerV2 by inject()

    companion object {
        private const val EXTRA_FOCUS_STATION = "extra_focus_station"
        fun createArguments(stationObject: StationObject): Bundle {
            return Bundle().apply { putParcelable(EXTRA_FOCUS_STATION, stationObject.toParcelable()) }
        }

        private fun MapFragment.focusStation() = arguments?.getParcelable<ParcelableStationObject>(EXTRA_FOCUS_STATION)?.toDomainObject()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        initMap()
        firebaseLogger.pageView(ViewIdentifier.MAP)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
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

    private fun initMap() {
        mapView.getMapAsync { map ->
            setupMap(map)
            getAndDrawStations(map)
        }
    }

    private fun setupMap(map: GoogleMap) {
        val hasFineLocationPermission = hasFineLocationPermission(requireContext())
        if (hasFineLocationPermission) {
            map.isMyLocationEnabled = true
        }

        MapsInitializer.initialize(activity)

        val focusLocation = focusStation()
        when {
            focusLocation != null -> positionCamera(map, LatLng(focusLocation.lat, focusLocation.longitude), 15.5f)
            hasFineLocationPermission -> {
                mapViewModel.locationLiveData.observe(this) { location ->
                    mapViewModel.locationLiveData.removeObservers(this)
                    positionCamera(map, LatLng(location.latitude, location.longitude), 15.5f)
                }
            }
            else -> positionCamera(map, LatLng(57.704728, 11.969011), 13f)
        }

    }

    private fun positionCamera(map: GoogleMap, latLng: LatLng, zoomLevel: Float) {
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(zoomLevel)
                .build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        map.moveCamera(cameraUpdate)
    }

    private fun getAndDrawStations(map: GoogleMap) {
        mapViewModel.stationObjects.observe(this) {stationObjectsResource ->
            when (stationObjectsResource) {
                Resource.Loading -> {}
                is Success -> drawMarkers(map, stationObjectsResource.body)
                is Failure -> Log.e("MapFragment", "Failed to fetch stations: ${stationObjectsResource.reason}")
            }
        }
    }

    private fun drawMarkers(map: GoogleMap, stations: List<StationObject>) {
        map.clear()
        val bikes = resources.getString(R.string.map_marker_snippet_bikes)
        val stands = resources.getString(R.string.map_marker_snippet_stands)
        val focusStations = focusStation()
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
}