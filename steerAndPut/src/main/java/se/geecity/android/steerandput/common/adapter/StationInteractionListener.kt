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
package se.geecity.android.steerandput.common.adapter

import se.geecity.android.domain.entities.StationObject
import se.geecity.android.steerandput.NavigationManager
import se.geecity.android.steerandput.common.logging.FirebaseLoggerV2
import se.geecity.android.steerandput.common.persistance.FavoriteUtil
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.mapv2.MapFragment

interface StationInteractionListener {
    fun onStationClicked(station: StationObject)
    fun onContextMenuDetailsClicked(station: StationObject)
    fun onContextMenuFavoriteToggled(station: StationObject)
}

class StationInteractionListenerImpl(
        private val firebaseLogger: FirebaseLoggerV2,
        private val favoriteUtil: FavoriteUtil
) : StationInteractionListener {
    override fun onStationClicked(station: StationObject) {
        firebaseLogger.stationListItemClicked(station)
        val navigationRequest = NavigationManager.NavigationRequest(ViewIdentifier.MAP,
                MapFragment.createArguments(station))
        NavigationManager.instance?.navigate(navigationRequest)
    }

    override fun onContextMenuDetailsClicked(station: StationObject) {
        firebaseLogger.stationListItemDetailsClicked(station)
        //TODO (widar): Navigate to station view. (STILL RELEVANT?)
    }

    override fun onContextMenuFavoriteToggled(station: StationObject) {
        if (favoriteUtil.isFavorite(station.id)) {
            favoriteUtil.removeFavorite(station.id)
            firebaseLogger.removeFavoriteStation(station, "context-menu")
        } else {
            favoriteUtil.addFavorite(station.id)
            firebaseLogger.addFavoriteStation(station, "context-menu")
        }
    }
}