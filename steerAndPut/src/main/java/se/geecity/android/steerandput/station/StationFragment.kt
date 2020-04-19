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
package se.geecity.android.steerandput.station

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_station.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import se.geecity.android.data.model.ParcelableStationObject
import se.geecity.android.data.model.toParcelable
import se.geecity.android.domain.entities.Resource
import se.geecity.android.domain.entities.StationObject
import se.geecity.android.domain.entities.Success
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.adapter.StationInteractionListener
import se.geecity.android.steerandput.common.logging.FirebaseLoggerV2
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.util.getDistanceBetweenAsString
import se.geecity.android.steerandput.common.util.hasCoarseLocationPermission
import se.geecity.android.steerandput.common.util.hasFineLocationPermission
import se.geecity.android.steerandput.common.view.StationShowingFragment
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.common.view.gone

//TODO remove the inheritence to station showing
class StationFragment : StationShowingFragment() {
    override var stations: List<Station> = listOf()

    private val station: StationObject by lazy { station() }
    private val stationViewModel: StationViewModel by viewModel()

    private val firebaseLogger: FirebaseLoggerV2 by inject()
    private val stationInteractionListener: StationInteractionListener by inject()

    companion object {
        private val EXTRA_STATION = "station"

        fun createArguments(station: StationObject): Bundle {
            return Bundle().apply { putParcelable(EXTRA_STATION, station.toParcelable()) }
        }

        private fun StationFragment.station(): StationObject {
            val station = arguments?.getParcelable<ParcelableStationObject>(EXTRA_STATION)?.toDomainObject()
            if (station != null) {
                return station
            }
            throw IllegalStateException("No station argument for StationFragment")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_station, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mapView.onCreate(savedInstanceState)

        setupMap()
        chart.gone()
        chartTitle.gone()

        setStationInfo(station)

        firebaseLogger.pageView(ViewIdentifier.STATION, station)
    }

    override fun onStart() {
        super.onStart()

        stationViewModel.location.observe(this) {
            setLocationText(it)
        }

        stationViewModel.stationObjectId = station.id
        stationViewModel.stationObject.observe(this) { stationResource ->
            when (stationResource) {
                Resource.Loading -> stationName.text = "LOADING"
                is Success -> setStationInfo(stationResource.body)
            }
        }
    }

    private fun setStationInfo(station: StationObject) {
        stationName.text = station.name
        stationBikeText.text = station.availableBikes.toString()
        stationStandsText.text = station.availableBikeStands.toString()
    }

    private fun setLocationText(location: Location) {
        stationDistance.text = getDistanceBetweenAsString(station, location)
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
                stationInteractionListener.onStationClicked(station)
                firebaseLogger.mapClicked(station)
            }
        }
    }
}