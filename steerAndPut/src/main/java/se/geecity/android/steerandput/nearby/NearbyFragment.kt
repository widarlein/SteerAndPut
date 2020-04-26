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
package se.geecity.android.steerandput.nearby

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_list.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import se.geecity.android.domain.entities.Failure
import se.geecity.android.domain.entities.Resource
import se.geecity.android.domain.entities.Success
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.adapter.StationAdapterV2
import se.geecity.android.steerandput.common.logging.FirebaseLoggerV2
import se.geecity.android.steerandput.common.util.hasFineLocationPermission
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.main.MainComm

class NearbyFragment : Fragment() {

    private val firebaseLogger: FirebaseLoggerV2 by inject()
    private val mainComm: MainComm by inject()

    private val nearbyViewModel: NearbyViewModel by viewModel()

    private val adapter: StationAdapterV2 by inject()
    private lateinit var connectionErrorView: View

    private val onLocation: (Location) -> Unit = { location ->
        adapter.location = location
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container,  false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseLogger.pageView(ViewIdentifier.NEARBY)
        recyclerView.adapter = adapter

        connectionErrorView = requireActivity().layoutInflater.inflate(R.layout.view_list_connection_error, list_listcontainer, false)
        connectionErrorView.visibility = View.GONE
        list_listcontainer.addView(connectionErrorView,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT))

        listSwipeRefreshLayout.setOnRefreshListener {
            nearbyViewModel.fetchStationObjects()
        }
    }

    private val mainObserver = object : MainComm.MainObserver {
        override fun locationPermissionGranted() {
            nearbyViewModel.locationLiveData.observe(this@NearbyFragment, onLocation)
        }
    }

    override fun onStart() {
        super.onStart()
        nearbyViewModel.stationObjects.observe(this) { stationObjectsResource ->
            when (stationObjectsResource) {
                Resource.Loading -> listSwipeRefreshLayout.isRefreshing = true
                is Success -> {
                    adapter.stations = stationObjectsResource.body
                    listSwipeRefreshLayout.isRefreshing = false
                    connectionErrorView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
                is Failure -> {
                    listSwipeRefreshLayout.isRefreshing = false
                    recyclerView.visibility = View.GONE
                    connectionErrorView.visibility = View.VISIBLE
                }
            }
        }

        if (hasFineLocationPermission(requireContext())) {
            nearbyViewModel.locationLiveData.observe(this, onLocation)
        }

        mainComm.addObserver(mainObserver)
    }

    override fun onStop() {
        super.onStop()
        mainComm.removeObserver(mainObserver)
    }
}