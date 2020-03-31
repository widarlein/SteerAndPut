package se.geecity.android.data.model

import com.google.gson.annotations.SerializedName
import se.geecity.android.domain.entities.StationObject

data class StationObjectDao(val stationId: Int,
                            val name: String,
                            val lat: Double,
                            @SerializedName("Long")
                          val longitude: Double,
                            val availableBikes: Int,
                            val availableBikeStands: Int,
                            val isOpen: Boolean) {
    fun toDomainObject(): StationObject = StationObject(stationId, name, lat, longitude, availableBikes, availableBikeStands, isOpen)
}