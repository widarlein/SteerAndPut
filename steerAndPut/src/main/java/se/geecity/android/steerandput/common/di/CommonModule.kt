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
package se.geecity.android.steerandput.common.di

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import se.geecity.android.data.AppExecutors
import se.geecity.android.steerandput.common.adapter.StationAdapterV2
import se.geecity.android.steerandput.common.adapter.StationInteractionListener
import se.geecity.android.steerandput.common.adapter.StationInteractionListenerImpl
import se.geecity.android.steerandput.common.logging.FirebaseLogger
import se.geecity.android.steerandput.common.logging.FirebaseLoggerV2
import se.geecity.android.steerandput.common.persistance.FavoriteUtil

val commonModule = module {

    single { FavoriteUtil(androidContext()) }
    single<FusedLocationProviderClient> { LocationServices.getFusedLocationProviderClient(androidContext()) }

    factory { StationAdapterV2(androidContext(), get()) }
    factory<StationInteractionListener> { StationInteractionListenerImpl(get(), get()) }

    factory { FirebaseAnalytics.getInstance(androidContext()) }
    single { FirebaseLogger(get(), androidContext()) }
    single { FirebaseLoggerV2(get(), androidContext()) }
    single { AppExecutors() }
}