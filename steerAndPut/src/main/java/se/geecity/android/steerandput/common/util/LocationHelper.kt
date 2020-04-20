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
package se.geecity.android.steerandput.common.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import se.geecity.android.domain.entities.StationObject
import java.text.DecimalFormat

private val decimalFormat: DecimalFormat = DecimalFormat("##.00")

fun getDistanceBetweenAsString(station: StationObject, otherLocation: Location): String {
    val result = FloatArray(1)
    Location.distanceBetween(station.lat, station.longitude, otherLocation.latitude, otherLocation.longitude, result)

    return getDistanceString(result[0])
}

private fun getDistanceString(meters: Float): String {
    val wholeMeters = meters.toInt()

    return if (wholeMeters >= 1000) {
        val floatDistance = wholeMeters / 1000f
        "${decimalFormat.format(floatDistance.toDouble())}  km"
    } else {
        "$wholeMeters m"
    }
}

fun hasFineLocationPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

fun hasCoarseLocationPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED