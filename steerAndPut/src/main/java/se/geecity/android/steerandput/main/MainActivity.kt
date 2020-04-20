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
package se.geecity.android.steerandput.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import se.geecity.android.data.AppExecutors
import se.geecity.android.steerandput.NavigationManager
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.constants.TAB_FAVORITES
import se.geecity.android.steerandput.common.constants.TAB_LIST
import se.geecity.android.steerandput.common.constants.TAB_MAP
import se.geecity.android.steerandput.common.logging.FirebaseLoggerV2
import se.geecity.android.steerandput.common.persistance.FavoriteUtil
import se.geecity.android.steerandput.common.util.hasFineLocationPermission
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.oss.OpenSourceLicensesDialog

private const val FINE_LOCATION_PERMISSION_REQUEST = 0

/**
 * The main activity of the application, acts as container for the fragments.
 */
class MainActivity : AppCompatActivity(),
        MainView,
        TabLayout.OnTabSelectedListener {

    private lateinit var favoriteUtil: FavoriteUtil

    private val mainPresenter: MainPresenter by inject()
    private val firebaseLogger: FirebaseLoggerV2 by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainPresenter.mainView = this

        favoriteUtil = FavoriteUtil(applicationContext, AppExecutors())

        setSupportActionBar(toolbar)

        NavigationManager.init(applicationContext, mainPresenter, supportFragmentManager)

        createTabs()
    }

    override fun onStart() {
        super.onStart()

        // Bad practice, I know. The beauty of open source is that i you can complain, you can fix it
        if (!hasFineLocationPermission(applicationContext)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_PERMISSION_REQUEST)
        }
    }

    override fun onStop() {
        super.onStop()
        NavigationManager.instance?.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.main_menu_refresh_text)
                .setIcon(R.drawable.ic_refresh)
                .setOnMenuItemClickListener {
                    refreshStations()
                    firebaseLogger.refreshButtonClicked()
                    true
                }
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu.add(R.string.main_menu_open_source_dialog)
                .setOnMenuItemClickListener {
                    val fragmentManager = supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    val prev = fragmentManager.findFragmentByTag("dialog_license")
                    if (prev != null) {
                        transaction.remove(prev)
                    }
                    transaction.addToBackStack(null)
                    OpenSourceLicensesDialog().show(fragmentManager, "dialog_license")
                    true
                }
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayout.removeOnTabSelectedListener(this)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val tag = tab.tag as String
        val (viewIdentifier, arguments) = when (tag) {
            TAB_FAVORITES -> Pair(ViewIdentifier.FAVORITES,
                    Bundle.EMPTY)
            TAB_LIST -> Pair(ViewIdentifier.NEARBY, Bundle.EMPTY)
            TAB_MAP -> Pair(ViewIdentifier.MAP,
                    Bundle.EMPTY)
            else -> Pair(ViewIdentifier.NEARBY, Bundle.EMPTY)
        }

        val navigationRequest = NavigationManager.NavigationRequest(viewIdentifier, arguments)
        NavigationManager.instance?.navigate(navigationRequest)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            FINE_LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                //TODO do nothing, I guess? TBD
                }
            }
        }
    }

    override fun selectFavoriteTab() {
        tabLayout.getTabAt(0)?.select()
    }

    override fun selectListTab() {
        tabLayout.getTabAt(1)?.select()
    }

    override fun selectMapTab() {
        tabLayout.getTabAt(2)?.select()
    }

    override fun markTabAsActiveWithoutEvent(viewIdentifier: ViewIdentifier) {
        //ugly hack is ugly
        val tab = when (viewIdentifier) {
            ViewIdentifier.FAVORITES -> tabLayout.getTabAt(0)
            ViewIdentifier.NEARBY -> tabLayout.getTabAt(1)
            ViewIdentifier.MAP -> tabLayout.getTabAt(2)
            else -> {
                tabLayout.newTab()
            }
        }
        tabLayout.removeOnTabSelectedListener(this)
        tab?.select()
        tabLayout.addOnTabSelectedListener(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    private fun createTabs() {
        val favoritesTab = tabLayout.newTab().apply {
            setText(R.string.main_tab_favorites_title)
            tag = TAB_FAVORITES
        }
        val listTab = tabLayout.newTab().apply {
            setText(R.string.main_tab_list_title)
            tag = TAB_LIST
        }
        val mapTab = tabLayout.newTab().apply {
            setText(R.string.main_tab_map_title)
            tag = TAB_MAP
        }

        tabLayout.addTab(favoritesTab, 0)
        tabLayout.addTab(listTab, 1)
        tabLayout.addTab(mapTab, 2)

        if (favoriteUtil.getFavorites().size > 0) {
            favoritesTab.select()
        } else {
            listTab.select()
        }

        tabLayout.addOnTabSelectedListener(this)
    }

    private fun refreshStations() {
        mainPresenter.refreshPressed()
    }
}