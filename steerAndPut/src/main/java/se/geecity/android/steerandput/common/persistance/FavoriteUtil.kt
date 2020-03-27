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
package se.geecity.android.steerandput.common.persistance

import android.content.Context
import android.content.SharedPreferences
import se.geecity.android.steerandput.common.exception.SteerAndPutException
import se.geecity.android.steerandput.common.model.Station

internal const val PREFS_FILENAME = "favorites"
internal const val PREFS_KEY = "favorites"

class FavoriteUtil(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    private val favorites: MutableSet<Int>

    init {
        val idsString = prefs.getString(PREFS_KEY, null)
        favorites = idsString?.split(",")?.map { it.toInt() }?.toMutableSet() ?: mutableSetOf()
    }

    fun getFavorites(): Set<Int> {
        return favorites
    }

    @Throws(SteerAndPutException::class)
    fun saveFavorites(favoritesIds: Set<Int>) {
        if (favoritesIds.isNotEmpty()) {
            val idsString = favoritesIds.joinToString(",")
            val editor = prefs.edit()
            editor.putString(PREFS_KEY, idsString)
            editor.apply()
        }
    }

    fun addFavorite(stationId: Int) {
        favorites.add(stationId)
        saveFavorites(favorites)
    }

    fun removeFavorite(stationId: Int) {
        favorites.remove(stationId)
        saveFavorites(favorites)
    }

    fun isFavorite(stationId: Int) = favorites.contains(stationId)
}