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
package se.geecity.android.steerandput.common.viewmodel

import androidx.lifecycle.MutableLiveData
import se.geecity.android.data.AppExecutors
import se.geecity.android.domain.entities.Resource
import se.geecity.android.domain.entities.StationObject
import se.geecity.android.domain.nearby.GetStationsObjects
import se.geecity.android.steerandput.main.MainComm

class StationObjectsGetterImpl(private val getStationsObjects: GetStationsObjects,
                               private val appExecutors: AppExecutors,
                               private val mainComm: MainComm) : StationObjectsGetter {

    init {
        mainComm.addObserver(object : MainComm.MainObserver {
            override fun refreshRequested() {
                stationObjects.value = Resource.Loading
                fetchStationObjects()
            }

        })
    }

    override val stationObjects: MutableLiveData<Resource<List<StationObject>>> by lazy {
        MutableLiveData<Resource<List<StationObject>>>().also {
            it.value = Resource.Loading
            appExecutors.worker.execute {
                val stations = getStationsObjects()
                stationObjects.postValue(stations)
            }
        }
    }

    override fun fetchStationObjects() {
        appExecutors.worker.execute {
            val stations = getStationsObjects.immediate()
            stationObjects.postValue(stations)
        }
    }
}