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
package se.geecity.android.steerandput.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.android.gms.location.FusedLocationProviderClient
import se.geecity.android.domain.entities.Resource
import se.geecity.android.domain.entities.StationObject
import se.geecity.android.domain.entities.Success
import se.geecity.android.steerandput.common.livedata.combineLatest
import se.geecity.android.steerandput.common.location.LocationLiveData
import se.geecity.android.steerandput.common.persistance.FavoriteLiveData
import se.geecity.android.steerandput.common.persistance.FavoriteUtil
import se.geecity.android.steerandput.common.viewmodel.StationObjectsGetter

class FavoriteViewModel(private val stationObjectsGetter: StationObjectsGetter,
                        private val favoriteUtil: FavoriteUtil,
                        fusedLocationProviderClient: FusedLocationProviderClient) : ViewModel(),
        StationObjectsGetter by stationObjectsGetter {

    private val favoriteLiveData = FavoriteLiveData(favoriteUtil)

    override val stationObjects: LiveData<Resource<List<StationObject>>>
        get() = stationObjectsGetter.stationObjects.combineLatest(favoriteLiveData)
                .map { transform ->
                    val (resource, favoriteIds) = transform
                    if (resource is Success) {
                        Success(resource.body.filter { favoriteIds.contains(it.id) })
                    } else {
                        resource
                    }
                }

    val location: LocationLiveData = LocationLiveData(fusedLocationProviderClient)
}