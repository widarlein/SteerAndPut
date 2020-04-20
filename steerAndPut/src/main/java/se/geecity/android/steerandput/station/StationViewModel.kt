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
package se.geecity.android.steerandput.station

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import se.geecity.android.data.AppExecutors
import se.geecity.android.domain.entities.Failure
import se.geecity.android.domain.entities.Resource
import se.geecity.android.domain.entities.StationObject
import se.geecity.android.domain.station.GetStationObject
import se.geecity.android.steerandput.common.location.LocationLiveData
import se.geecity.android.steerandput.main.MainComm

class StationViewModel(private val getStationsObject: GetStationObject,
                       private val appExecutors: AppExecutors,
                       private val mainComm: MainComm,
                       fusedLocationProviderClient: FusedLocationProviderClient) : ViewModel() {

    init {
        mainComm.addObserver(object : MainComm.MainObserver {
            override fun refreshRequested() {
                stationObject.value = Resource.Loading
                fetchStationObject()
            }

        })
    }

    var stationObjectId: Int? = null

    val location = LocationLiveData(fusedLocationProviderClient)

    val stationObject: MutableLiveData<Resource<StationObject>> by lazy {
        MutableLiveData<Resource<StationObject>>().also {
            it.value = Resource.Loading

            val stationObjectId = stationObjectId
            if (stationObjectId != null) {
                appExecutors.worker.execute {
                    val station = getStationsObject(stationObjectId)
                    it.postValue(station)
                }
            } else {
                it.postValue(Failure("No station object ID present on getting station object"))
            }
        }
    }

    fun fetchStationObject() {
        stationObject.value = Resource.Loading
        makeStationObjectFetch()
    }

    private fun makeStationObjectFetch() {
        val stationObjectId = stationObjectId
        if (stationObjectId != null) {
            appExecutors.worker.execute {
                val station = getStationsObject(stationObjectId)
                stationObject.postValue(station)
            }
        } else {
            stationObject.postValue(Failure("No station object ID present on getting station object"))
        }
    }
}