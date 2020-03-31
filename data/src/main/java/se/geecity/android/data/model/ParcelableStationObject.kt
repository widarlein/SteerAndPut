package se.geecity.android.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.geecity.android.domain.entities.StationObject

@Parcelize
data class ParcelableStationObject(val stationId: Int,
                                   val name: String,
                                   val lat: Double,
                                   val longitude: Double,
                                   val availableBikes: Int,
                                   val availableBikeStands: Int,
                                   val isOpen: Boolean) : Parcelable {
    fun toDomainObject(): StationObject = StationObject(stationId, name, lat, longitude, availableBikes, availableBikeStands, isOpen)
}
fun StationObject.toParcelable() : ParcelableStationObject = ParcelableStationObject(id, name, lat, longitude, availableBikes, availableBikeStands, isOpen)