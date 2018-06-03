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
package se.geecity.android.steerandput.common.provider

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.historicalstation.model.HistoricalStation
import java.io.IOException
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Client for stations from the Styr & Ställ open data service SelfServiceBicyleService
 * provided by Gothenburg municipality.
 *
 * @param apiKey api key for the SelfServiceBicycleService service.
 */
class SelfServiceBicycleServiceProviderImpl(private val apiKey: String) : SelfServiceBicycleServiceProvider {

    companion object {
        private val URL_TEMPLATE = "http://data.goteborg.se/SelfServiceBicycleService/v1.0/Stations/%s?format=json"
    }

    private val url = String.format(URL_TEMPLATE, apiKey)
    private val dateFormat: DateFormat = SimpleDateFormat("YYYY-MM-dd")

    private val stationListTypeToken = object : TypeToken<List<Station>>() {}
    private val historicalStationListTypeToken = object : TypeToken<List<HistoricalStation>>() {}

    private var gson: Gson
    private val client = OkHttpClient()

    init {
        val deserializer = object : JsonDeserializer<Date> {
            override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date? {
                val raw = json?.asJsonPrimitive?.asString ?: return null
                val timeStampString = raw.substring(6..18)
                return Date(timeStampString.toLong())
            }
        }
        gson = GsonBuilder().registerTypeAdapter(Date::class.java, deserializer)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create()
    }

    override fun fetchStations(success: (stations: List<Station>) -> Unit, failure: (error: String, throwable: Throwable?) -> Unit) {

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException?) {
                failure(e.toString(), e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    //TODO JsonSyntaxException (on wifis redirecting you to login page, eg)
                    val stations: List<Station> = gson.fromJson(response.body().string(), stationListTypeToken.type)
                    success(stations)
                } else {
                    failure(response.body().string(), null)
                }
            }
        })
    }

    override fun fetchStationHistory(stationId: Int, from: Date, to: Date, success: (List<HistoricalStation>) -> Unit,
                                     failure: (error: String, throwable: Throwable?) -> Unit) {

        val url = buildHistoricalStationUrl(stationId, from, to)

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                failure(e?.message!!, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val historicalStations: List<HistoricalStation> = gson.fromJson(response.body().string(), historicalStationListTypeToken.type)
                    success(historicalStations)
                } else {
                    failure(response.body().string(), null)
                }
            }
        })
    }

    private fun buildHistoricalStationUrl(stationId: Int, from: Date, to: Date): String = buildString {
        append("http://data.goteborg.se/SelfServiceBicycleService/v1.0/HistoricalData/")
        append(apiKey) //TODO Change to injected variable
        append("/")
        append(stationId)
        append("/")
        append(dateFormat.format(from))
        append("/")
        append(dateFormat.format(to))
        append("?format=json")
    }
}