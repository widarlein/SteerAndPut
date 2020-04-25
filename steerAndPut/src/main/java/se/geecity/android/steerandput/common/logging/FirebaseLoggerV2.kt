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
import se.geecity.android.domain.entities.StationObject
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.view.ViewIdentifier

class FirebaseLoggerV2(private val firebaseAnalytics: FirebaseAnalytics, private val context: Context) {

    fun pageView(viewIdentifier: ViewIdentifier, stationObject: StationObject? = null) {
        val bundle = Bundle()
        bundle.putString(VIEW_ID, viewIdentifier.toString())

        if (stationObject != null) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, stationObject.id.toString())
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, stationObject.name)
        }

        firebaseAnalytics.logEvent(PAGE_VIEW, bundle)
    }

    fun stationListItemClicked(StationObject: StationObject) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, StationObject.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, StationObject.name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "StationObject")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun stationListItemDetailsClicked(StationObject: StationObject) {
        val detailsString = context.resources.getString(R.string.station_list_fragment_context_details)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, StationObject.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, detailsString)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "context-menu-item")
        bundle.putString(FirebaseAnalytics.Param.CONTENT, StationObject.name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun removeFavoriteStation(StationObject: StationObject, source: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, StationObject.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, StationObject.name)
        firebaseAnalytics.logEvent(REMOVE_FAVORITE, bundle)
    }

    fun addFavoriteStation(StationObject: StationObject, source: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, StationObject.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, StationObject.name)
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

    fun mapClicked(StationObject: StationObject) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, StationObject.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "map-view")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "layout")
        bundle.putString(FirebaseAnalytics.Param.CONTENT, StationObject.name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun stationDetailsView() {
        firebaseAnalytics.logEvent(STATION_DETAILS_VIEW, null)
    }
}