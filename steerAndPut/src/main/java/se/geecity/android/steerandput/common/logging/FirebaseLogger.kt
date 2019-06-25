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
package se.geecity.android.steerandput.common.logging

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.view.ViewIdentifier

class FirebaseLogger(private val firebaseAnalytics: FirebaseAnalytics, private val context: Context) {

    fun pageView(viewIdentifier: ViewIdentifier, station: Station? = null) {
        val bundle = Bundle()
        bundle.putString(VIEW_ID, viewIdentifier.toString())

        if (station != null) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, station.id.toString())
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, station.name)
        }

        firebaseAnalytics.logEvent(PAGE_VIEW, bundle)
    }

    fun stationListItemClicked(station: Station) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, station.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, station.name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "station")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun stationListItemDetailsClicked(station: Station) {
        val detailsString = context.resources.getString(R.string.station_list_fragment_context_details)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, station.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, detailsString)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "context-menu-item")
        bundle.putString(FirebaseAnalytics.Param.CONTENT, station.name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun removeFavoriteStation(station: Station, source: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, station.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, station.name)
        firebaseAnalytics.logEvent(REMOVE_FAVORITE, bundle)
    }

    fun addFavoriteStation(station: Station, source: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, station.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, station.name)
        firebaseAnalytics.logEvent(ADD_FAVORITE, bundle)
    }

    fun refreshButtonClicked() {
        val refreshName = context.resources.getString(R.string.main_menu_refresh_text)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, refreshName)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, refreshName)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "options-menu-item")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun swipeRefreshReleased() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "swipe-refresh-layout")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "swipe-refresh-layout")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "layout")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun mapClicked(station: Station) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, station.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "map-view")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "layout")
        bundle.putString(FirebaseAnalytics.Param.CONTENT, station.name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

}