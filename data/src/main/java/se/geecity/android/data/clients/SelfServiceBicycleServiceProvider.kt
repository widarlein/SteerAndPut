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
package se.geecity.android.data.clients

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import se.geecity.android.data.AppExecutors
import se.geecity.android.data.model.StationObjectDao
import se.geecity.android.domain.entities.Failure
import se.geecity.android.domain.entities.Resource
import se.geecity.android.domain.entities.StationObject
import se.geecity.android.domain.entities.Success
import se.geecity.android.domain.repositories.StationObjectRepository
import java.io.IOException
import java.lang.reflect.Type
import java.util.*

/**
 * Client for stations from the Styr & Ställ open data service SelfServiceBicyleService
 * provided by Gothenburg municipality.
 *
 * @param apiKey api key for the SelfServiceBicycleService service.
 */
class SelfServiceBicycleServiceProvider(apiKey: String, private val appExecutors: AppExecutors) : StationObjectRepository {

    companion object {
        private val URL_TEMPLATE = "https://data.goteborg.se/SelfServiceBicycleService/v2.0/Stations/%s?format=json"
    }

    private val url = String.format(URL_TEMPLATE, apiKey)

    private val stationListTypeToken = object : TypeToken<List<StationObjectDao>>() {}

    private val gson: Gson
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

    override fun getStationObjects(): Resource<List<StationObject>> {
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            return if (response.isSuccessful) {
                //TODO JsonSyntaxException (on wifis redirecting you to login page, eg)
                val stationDaos: List<StationObjectDao> = gson.fromJson(response.body()!!.string(), stationListTypeToken.type)

                Success(stationDaos.map { it.toDomainObject() })

            } else {
                Failure(response.body()!!.string())
            }

        } catch (e: IOException) {
            return Failure(e.message)
        }
    }
}