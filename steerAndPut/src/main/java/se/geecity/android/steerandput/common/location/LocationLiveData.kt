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
package se.geecity.android.steerandput.common.location

import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class LocationLiveData(private val fusedLocationProviderClient: FusedLocationProviderClient) : LiveData<Location>() {

    companion object {
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 200
        }
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onActive() {
        super.onActive()
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {location: Location? ->
            location?.let { value = location }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { value = it }
        }
    }
}