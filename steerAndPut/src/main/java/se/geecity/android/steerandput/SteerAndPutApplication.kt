/*
 * MIT License
 * 
 * Copyright (c) 2018 Alexander Widar
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
package se.geecity.android.steerandput

import android.app.Application
import org.koin.android.ext.android.setProperty
import org.koin.android.ext.android.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import se.geecity.android.steerandput.common.constants.BICYCLESERVICE_API_KEY_PROPERTY
import se.geecity.android.steerandput.historicalstation.di.stationModule
import se.geecity.android.steerandput.main.di.mainModule

/**
 * Application class of the app. Used for initializing Koin
 */
class SteerAndPutApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(mainModule, stationModule))
        initKoinProperties()
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }

    private fun initKoinProperties() {
        setProperty(BICYCLESERVICE_API_KEY_PROPERTY, BuildConfig.BICYCLESERVICE_API_KEY)
    }
}