package se.geecity.android.steerandput.common.logging

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import se.geecity.android.steerandput.common.model.Station

class FirebaseLogger(private val firebaseAnalytics: FirebaseAnalytics) {

    fun stationListItemClicked(station: Station) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, station.id.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, station.name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "station")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

}