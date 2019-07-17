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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import com.google.android.material.tabs.TabLayout
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import se.geecity.android.steerandput.NavigationManager
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.constants.ACTION_BROADCAST_NEW_LOCATION
import se.geecity.android.steerandput.common.constants.ACTION_FINISHED_REFRESHING_STATIONS
import se.geecity.android.steerandput.common.constants.ACTION_IS_REFRESHING_STATIONS
import se.geecity.android.steerandput.common.constants.ACTION_REFRESHING_STATIONS_ERROR
import se.geecity.android.steerandput.common.constants.ACTION_REQUEST_REFRESH_STATIONS
import se.geecity.android.steerandput.common.constants.EXTRA_BROADCAST_LOCATION
import se.geecity.android.steerandput.common.constants.EXTRA_BROADCAST_STATIONS
import se.geecity.android.steerandput.common.constants.EXTRA_ERROR_MESSAGE
import se.geecity.android.steerandput.common.constants.TAB_FAVORITES
import se.geecity.android.steerandput.common.constants.TAB_LIST
import se.geecity.android.steerandput.common.constants.TAB_MAP
import se.geecity.android.steerandput.common.logging.FirebaseLogger
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.persistance.FavoriteUtil
import se.geecity.android.steerandput.common.util.hasFineLocationPermission
import se.geecity.android.steerandput.common.view.ViewIdentifier
import se.geecity.android.steerandput.map.MapFragment
import se.geecity.android.steerandput.oss.OpenSourceLicensesDialog
import se.geecity.android.steerandput.stationlist.favorite.FavoritesFragment
import se.geecity.android.steerandput.stationlist.list.ListFragment
import java.util.ArrayList

private const val FINE_LOCATION_PERMISSION_REQUEST = 0
private const val TAG = "MainActivity"

/**
 * The main activity of the application, acts as container for the fragments.
 */
class MainActivity : AppCompatActivity(),
        MainView,
        TabLayout.OnTabSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private var stations: List<Station> = arrayListOf()

    private lateinit var googleApiClient: GoogleApiClient
    private var location: Location? = null

    private lateinit var localBroadcasManager: LocalBroadcastManager
    private lateinit var favoriteUtil: FavoriteUtil

    private val mainPresenter: MainPresenter by inject()
    private val firebaseLogger: FirebaseLogger by inject()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_REQUEST_REFRESH_STATIONS -> refreshStations()
            }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            this@MainActivity.location = location
            val intent = Intent(ACTION_BROADCAST_NEW_LOCATION)
            intent.putExtra(EXTRA_BROADCAST_LOCATION, location)
            localBroadcasManager.sendBroadcast(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainPresenter.mainView = this

        localBroadcasManager = LocalBroadcastManager.getInstance(applicationContext)
        favoriteUtil = FavoriteUtil(applicationContext)

        setSupportActionBar(toolbar)

        buildGoogleApiClient()

        NavigationManager.init(applicationContext, mainPresenter, supportFragmentManager)

        createTabs()
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(ACTION_REQUEST_REFRESH_STATIONS)
        localBroadcasManager.registerReceiver(broadcastReceiver, intentFilter)

        googleApiClient.connect()
        refreshStations()
    }

    override fun onStop() {
        super.onStop()
        localBroadcasManager.unregisterReceiver(broadcastReceiver)
        if (googleApiClient.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener)
        }
        googleApiClient.disconnect()
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

    override fun newStations(stations: List<Station>) {
        this@MainActivity.stations = stations

        val intent = Intent(ACTION_FINISHED_REFRESHING_STATIONS).apply {
            putParcelableArrayListExtra(EXTRA_BROADCAST_STATIONS, stations as ArrayList<out Parcelable>)
        }
        localBroadcasManager.sendBroadcast(intent)
    }

    override fun onStationError(error: String, throwable: Throwable?) {
        sendErrorBroadcast(throwable ?: Throwable(error))
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val tag = tab.tag as String
        val (viewIdentifier, arguments) = when (tag) {
            TAB_FAVORITES -> Pair(ViewIdentifier.FAVORITES,
                    FavoritesFragment.createArgumentsBundle(stations, location))
            TAB_LIST -> Pair(ViewIdentifier.LIST,
                    ListFragment.createArgumentsBundle(stations, location))
            TAB_MAP -> Pair(ViewIdentifier.MAP,
                    MapFragment.createArgumentsBundle(stations, location))
            else -> Pair(ViewIdentifier.LIST,
                    ListFragment.createArgumentsBundle(stations, location))
        }

        val navigationRequest = NavigationManager.NavigationRequest(viewIdentifier, arguments)
        NavigationManager.instance?.navigate(navigationRequest)
    }

    override fun onConnected(connectionHint: Bundle?) {
        if (!hasFineLocationPermission(applicationContext)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_PERMISSION_REQUEST)
        } else {
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            FINE_LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                }
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "Location service connection failure: $connectionResult")
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
            ViewIdentifier.LIST -> tabLayout.getTabAt(1)
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

    override fun onConnectionSuspended(i: Int) {}

    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

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

        if (favoriteUtil.favorites.size > 0) {
            favoritesTab.select()
        } else {
            listTab.select()
        }

        tabLayout.addOnTabSelectedListener(this)
    }

    private fun refreshStations() {
        val intent = Intent(ACTION_IS_REFRESHING_STATIONS)
        localBroadcasManager.sendBroadcast(intent)
        mainPresenter.refreshPressed()
    }

    private fun sendErrorBroadcast(throwable: Throwable) {
        val intent = Intent(ACTION_REFRESHING_STATIONS_ERROR)
        intent.putExtra(EXTRA_ERROR_MESSAGE, throwable.localizedMessage)
        localBroadcasManager.sendBroadcast(intent)
    }

    private fun startLocationUpdates() {
        try {
            val lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            location = lastLocation
            if (lastLocation != null) {
                locationListener.onLocationChanged(lastLocation)
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                    buildLocationRequest(), locationListener)
        } catch (securityException: SecurityException) {
            // This should not happen.
        }
    }

    private fun buildLocationRequest() = LocationRequest().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 5000
        fastestInterval = 200
    }
}