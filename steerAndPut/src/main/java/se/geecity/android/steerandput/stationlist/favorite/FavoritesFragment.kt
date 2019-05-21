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

import android.location.Location
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_list.*
import org.koin.android.ext.android.inject
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.logging.FirebaseLogger
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.view.StationShowingFragment
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.stationlist.AbsListFragment

/**
 * Showing list of favorited stations
 */
class FavoritesFragment : AbsListFragment() {

    override var stations: List<Station> = mutableListOf()
    set(value) {
        field = value
        updateList()
    }

    private lateinit var emptyView: EmptyView
    private val firebaseLogger: FirebaseLogger by inject()

    companion object {
        fun createArgumentsBundle(stations: List<Station>?, location: Location?): Bundle {
            return StationShowingFragment.createArguments(stations, location)
        }

        fun newFragment(stations: List<Station>?, location: Location?): FavoritesFragment =
                FavoritesFragment()
                        .apply { arguments = createArgumentsBundle(stations, location) }
    }

    override fun initialize() {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragmet_favorites_emptyview, recyclerView, false)
        emptyView = EmptyView(view)
        adapter.adapterDataObserver = emptyView.adapterDataObserver
        list_listcontainer.addView(emptyView.mView, 0)

        if (adapter.stations.isEmpty()) {
            emptyView.mView.visibility = View.VISIBLE
        }
    }

    override fun parseArguments() {
        super.parseArguments()
        //TODO make this less confusing. if stations is not null, it means that
        // the fragment has been popped from backstack
        val stations = StationShowingFragment.parseStationsArgument(arguments!!)
        if (stations != null) {
            this.stations = stations.toList()
        } else if (!this.stations.isEmpty()) {
            updateList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseLogger.pageView(ViewIdentifier.FAVORITES)
    }

    override fun onStart() {
        super.onStart()
        if (stations.isEmpty()) {
            listSwipeRefreshLayout.isRefreshing = true
            requestStations()
        }
    }

    override fun updateList() {
        val favorites = favoriteUtil.favorites
        adapter.favorites = favorites
        adapter.stations = stations.filter { favorites.contains(it.id) }
    }

    override fun onNetworkError() {
        super.onNetworkError()
        emptyView.mView.visibility = View.GONE
    }
}