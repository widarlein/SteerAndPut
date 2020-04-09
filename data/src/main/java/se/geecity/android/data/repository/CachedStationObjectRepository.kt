package se.geecity.android.data.repository

import android.os.SystemClock
import se.geecity.android.data.clients.SelfServiceBicycleServiceProvider
import se.geecity.android.domain.entities.Resource
import se.geecity.android.domain.entities.StationObject
import se.geecity.android.domain.repositories.StationObjectRepository

class CachedStationObjectRepository(private val networkRepository: SelfServiceBicycleServiceProvider) : StationObjectRepository {

    private var cache: Resource<List<StationObject>>? = null
    private var lastRequestTime: Long = 0

    override fun getStationObjects(): Resource<List<StationObject>> {

        val now = SystemClock.elapsedRealtime()
        val cache = cache

        val resource = if (now - lastRequestTime < 30000 && cache != null) {
            cache
        } else {
            networkRepository.getStationObjects().also { this.cache = it }
        }

        lastRequestTime = now
        return resource
    }
}