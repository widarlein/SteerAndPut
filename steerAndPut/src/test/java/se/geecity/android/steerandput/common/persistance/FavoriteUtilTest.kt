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
package se.geecity.android.steerandput.common.persistance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.geecity.android.data.AppExecutors

private const val FAVORITES_IDS_STRING = "1,2,5,6"

@RunWith(RobolectricTestRunner::class)
class FavoriteUtilTest {

    private val expectedFavorites = setOf(1, 2, 5, 6)
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun getFavorites_noFavs() {
        val underTest = FavoriteUtil(context, AppExecutors())
        val favorites = underTest.getFavorites()
        assertThat(favorites).isEqualTo(setOf<Int>())
    }

    @Test
    fun getFavorites_withFavs() {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREFS_KEY, FAVORITES_IDS_STRING).commit()

        val underTest = FavoriteUtil(context, AppExecutors())
        val favorites = underTest.getFavorites()
        assertThat(favorites).isEqualTo(expectedFavorites)
    }

    @Test
    fun saveFavorites_empty() {
        val underTest = FavoriteUtil(context, AppExecutors())
        underTest.saveFavorites(setOf())

        val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val favoriteString = prefs.getString(PREFS_KEY, null)
        assertThat(favoriteString).isNull()
    }
}