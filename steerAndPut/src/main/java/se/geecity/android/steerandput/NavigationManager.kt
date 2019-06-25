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
package se.geecity.android.steerandput

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import android.util.Log
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.persistance.FavoriteUtil
import se.geecity.android.steerandput.common.view.StationShowingFragment
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.historicalstation.StationFragment
import se.geecity.android.steerandput.main.MainPresenter
import se.geecity.android.steerandput.map.MapFragment
import se.geecity.android.steerandput.stationlist.favorite.FavoritesFragment
import se.geecity.android.steerandput.stationlist.list.ListFragment
import java.lang.IllegalStateException

class NavigationManager private constructor(context: Context,
                                            private val fragmentManager: FragmentManager,
                                            private val mainPresenter: MainPresenter) : FragmentManager.OnBackStackChangedListener {

    private val favoritsUtil = FavoriteUtil(context)

    init {
        setInitialFragment(favoritsUtil.favorites)
    }

    companion object {

        fun init(context: Context, mainPresenter: MainPresenter, fragmentManager: FragmentManager) {
            instance = NavigationManager(context, fragmentManager, mainPresenter)
        }
        var instance: NavigationManager? = null
    }
    fun navigate(navigationRequest: NavigationRequest) {
        when (navigationRequest.viewIdentifier) {
            ViewIdentifier.LIST -> navigateToFragment(ListFragment().apply { arguments = navigationRequest.arguments }, navigationRequest.viewIdentifier)
            ViewIdentifier.MAP -> navigateToFragment(MapFragment().apply { arguments = navigationRequest.arguments }, navigationRequest.viewIdentifier)
            ViewIdentifier.FAVORITES -> navigateToFragment(FavoritesFragment().apply { arguments = navigationRequest.arguments }, navigationRequest.viewIdentifier)
            ViewIdentifier.STATION -> navigateToFragment(StationFragment().apply { arguments = navigationRequest.arguments }, navigationRequest.viewIdentifier)
        }
    }

    override fun onBackStackChanged() {
        val fragments = fragmentManager.fragments
        if (fragments.isNotEmpty()) {
            val fragment = fragments.first()
            val viewIdentifier = viewIdentifierFromTag(fragment.tag)
            mainPresenter.viewActiveFromBackstack(viewIdentifier)
        }
    }

    private fun navigateToFragment(fragment: StationShowingFragment, viewIdentifier: ViewIdentifier) {

        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment, viewIdentifier.toString())
        transaction.addToBackStack(null)
        transaction.commit()

        fragmentManager.executePendingTransactions()
    }

    private fun setInitialFragment(favorites: Set<Int>) {

        val (fragment, tag) = if (!favorites.isEmpty()) {

            FavoritesFragment.newFragment(null, null) to ViewIdentifier.FAVORITES.toString()

        } else {

            ListFragment.newFragment(null, null) to ViewIdentifier.LIST.toString()

        }

        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment, tag)
        transaction.commit()
        fragmentManager.executePendingTransactions()

        fragmentManager.addOnBackStackChangedListener(this)
    }

    private fun viewIdentifierFromTag(tag: String?): ViewIdentifier =
            if (tag != null) {
                ViewIdentifier.valueOf(tag)
            } else {
                throw IllegalStateException("The fragment tag should not be null")
            }

    fun stop() {
        fragmentManager.removeOnBackStackChangedListener(this)
    }

    class NavigationRequest(val viewIdentifier: ViewIdentifier, val arguments: Bundle)
}