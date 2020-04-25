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
import com.google.android.material.bottomnavigation.BottomNavigationView
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
        MainView {

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

        initBottomNavigation()
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
                    mainPresenter.refreshPressed()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            FINE_LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                //TODO do nothing, I guess? TBD
                }
            }
        }
    }


    override fun markTabAsActiveWithoutEvent(viewIdentifier: ViewIdentifier) {
        //ugly hack is ugly
        val itemId = when (viewIdentifier) {
            ViewIdentifier.FAVORITES -> R.id.nav_favorites
            ViewIdentifier.NEARBY -> R.id.nav_nearby
            ViewIdentifier.MAP -> R.id.nav_map
            else -> R.id.nav_nearby
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(null)
        bottomNavigationView.selectedItemId = itemId
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private fun initBottomNavigation() {

        val navId = if (favoriteUtil.getFavorites().isEmpty()) {
            R.id.nav_nearby
        } else R.id.nav_favorites

        bottomNavigationView.selectedItemId = navId
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    val onNavigationItemSelectedListener =  BottomNavigationView.OnNavigationItemSelectedListener {item: MenuItem ->
        val viewIdentifier = when (item.itemId) {
            R.id.nav_favorites -> ViewIdentifier.FAVORITES
            R.id.nav_nearby -> ViewIdentifier.NEARBY
            R.id.nav_map -> ViewIdentifier.MAP
            else -> ViewIdentifier.NEARBY
        }

        val request = NavigationManager.NavigationRequest(viewIdentifier, Bundle.EMPTY)
        NavigationManager.instance?.navigate(request)
        true
    }
}