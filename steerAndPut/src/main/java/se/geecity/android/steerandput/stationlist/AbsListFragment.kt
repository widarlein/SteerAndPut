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
package se.geecity.android.steerandput.stationlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.fragment_list.*
import org.koin.android.ext.android.inject
import se.geecity.android.steerandput.NavigationManager
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.constants.ACTION_BROADCAST_NEW_LOCATION
import se.geecity.android.steerandput.common.constants.ACTION_FINISHED_REFRESHING_STATIONS
import se.geecity.android.steerandput.common.constants.ACTION_IS_REFRESHING_STATIONS
import se.geecity.android.steerandput.common.constants.ACTION_REFRESHING_STATIONS_ERROR
import se.geecity.android.steerandput.common.constants.ACTION_REQUEST_REFRESH_STATIONS
import se.geecity.android.steerandput.common.constants.EXTRA_BROADCAST_LOCATION
import se.geecity.android.steerandput.common.constants.EXTRA_BROADCAST_STATIONS
import se.geecity.android.steerandput.common.logging.FirebaseLogger
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.persistance.FavoriteUtil
import se.geecity.android.steerandput.common.view.StationShowingFragment
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.historicalstation.StationFragment
import se.geecity.android.steerandput.map.MapFragment
import se.geecity.android.steerandput.stationlist.adapter.StationAdapter
import java.util.ArrayList

abstract class AbsListFragment : StationShowingFragment() {

    companion object {
        const val STATE_EXTRA_STATIONS = "state_extra_stations"

        fun parseLocationArgument(bundle: Bundle) = bundle.getParcelable<Location>(StationShowingFragment.ARG_LOCATION)
    }

    protected lateinit var rootView: View
    protected lateinit var connectionErrorView: View
    protected lateinit var localBroadcastManager: LocalBroadcastManager
    protected lateinit var adapter: StationAdapter
    protected lateinit var favoriteUtil: FavoriteUtil

    private val firebaseLogger: FirebaseLogger by inject()

    override var stations: List<Station> = listOf()
    private var location: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_list, container, false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    protected fun init() {
        adapter = StationAdapter(context!!, stationInteractionListener)

        recyclerView.adapter = adapter
        listSwipeRefreshLayout.setOnRefreshListener(onRefreshListener)

        localBroadcastManager = LocalBroadcastManager.getInstance(context!!)
        favoriteUtil = FavoriteUtil(context)

        parseArguments()

        connectionErrorView = activity!!.layoutInflater.inflate(R.layout.view_list_connection_error, list_listcontainer, false)
        connectionErrorView.visibility = View.GONE
        list_listcontainer.addView(connectionErrorView,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT))

        registerForContextMenu(recyclerView)

        initialize()
    }

    protected abstract fun initialize()

    protected abstract fun updateList()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            val stations = savedInstanceState.getParcelableArrayList<Station>(STATE_EXTRA_STATIONS)
            this.stations = stations
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction(ACTION_IS_REFRESHING_STATIONS)
        filter.addAction(ACTION_FINISHED_REFRESHING_STATIONS)
        filter.addAction(ACTION_REFRESHING_STATIONS_ERROR)
        filter.addAction(ACTION_BROADCAST_NEW_LOCATION)
        localBroadcastManager.registerReceiver(broadcastReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //TODO (widar): break out to helper class and add error handling if 0 stations. Seems to cause ClassCastException on some devices.
        outState.putParcelableArrayList(STATE_EXTRA_STATIONS, stations as ArrayList<out Parcelable>)
    }

    protected open fun onNetworkError() {
        listSwipeRefreshLayout.isRefreshing = true
        showError()
    }

    private fun showError() {
        recyclerView.visibility = View.GONE
        connectionErrorView.visibility = View.VISIBLE
    }

    private fun hideErrorIfVisible() {
        connectionErrorView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    protected fun requestStations() {
        val intent = Intent(ACTION_REQUEST_REFRESH_STATIONS)
        localBroadcastManager.sendBroadcast(intent)
    }

    protected open fun parseArguments() {
        val location = parseLocationArgument(arguments!!)
        if (location != null) {
            this.location = location
            adapter.location = location
        } else if (this.location != null) {
            adapter.location = this.location
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {
                ACTION_IS_REFRESHING_STATIONS -> listSwipeRefreshLayout.isRefreshing = true
                ACTION_FINISHED_REFRESHING_STATIONS -> {
                    listSwipeRefreshLayout.isRefreshing = false
                    val stations = intent.getParcelableArrayListExtra<Station>(EXTRA_BROADCAST_STATIONS)
                    this@AbsListFragment.stations = stations
                    hideErrorIfVisible()
                }
                ACTION_REFRESHING_STATIONS_ERROR -> onNetworkError()
                ACTION_BROADCAST_NEW_LOCATION -> {
                    val location = intent.getParcelableExtra<Location>(EXTRA_BROADCAST_LOCATION)
                    this@AbsListFragment.location = location
                    adapter.location = location
                }
            }
        }
    }

    private val onRefreshListener = object : SwipeRefreshLayout.OnRefreshListener {
        override fun onRefresh() {
            requestStations()
            firebaseLogger.swipeRefreshReleased()
        }
    }

    private val stationInteractionListener = object : StationAdapter.StationInteractionListener {
        override fun onStationClicked(station: Station) {
            firebaseLogger.stationListItemClicked(station)
            val request = NavigationManager.NavigationRequest(ViewIdentifier.MAP, MapFragment.createArgumentsBundle(stations, location, station))
            NavigationManager.instance?.navigate(request)
        }

        override fun onContextMenuDetailsClicked(station: Station) {
            firebaseLogger.stationListItemDetailsClicked(station)
            val request = NavigationManager.NavigationRequest(ViewIdentifier.STATION, StationFragment.createArgumentBundle(stations, station, location))
            NavigationManager.instance?.navigate(request)
        }

        override fun onContextMenuFavoriteToggled(station: Station) {
            if (favoriteUtil.favorites.contains(station.id)) {
                favoriteUtil.removeFavorite(station.id)
                firebaseLogger.removeFavoriteStation(station, "context-menu")
            } else {
                favoriteUtil.addFavorite(station.id)
                firebaseLogger.addFavoriteStation(station, "context-menu")
            }
            updateList()
        }
    }
}