package se.geecity.android.steerandput.common.persistance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val FAVORITES_IDS_STRING = "1,2,5,6"

@RunWith(RobolectricTestRunner::class)
class NewFavoritesUtilTest {

    private val expectedFavorites = setOf(1, 2, 5, 6)
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun getFavorites_noFavs() {
        val underTest = NewFavoritesUtil(context)
        val favorites = underTest.getFavorites()
        assertThat(favorites).isEqualTo(setOf<Int>())
    }

    @Test
    fun getFavorites_withFavs() {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREFS_KEY, FAVORITES_IDS_STRING).commit()

        val underTest = NewFavoritesUtil(context)
        val favorites = underTest.getFavorites()
        assertThat(favorites).isEqualTo(expectedFavorites)
    }

    @Test
    fun saveFavorites_empty() {
        val underTest = NewFavoritesUtil(context)
        underTest.saveFavorites(setOf())

        val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val favoriteString = prefs.getString(PREFS_KEY, null)
        assertThat(favoriteString).isNull()
    }
}