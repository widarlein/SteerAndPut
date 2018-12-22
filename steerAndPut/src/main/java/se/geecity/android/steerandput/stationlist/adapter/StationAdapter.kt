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
package se.geecity.android.steerandput.stationlist.adapter

import android.content.Context
import android.location.Location
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_station_list_item.view.*
import se.geecity.android.steerandput.R
import se.geecity.android.steerandput.common.model.Station
import se.geecity.android.steerandput.common.util.getDistanceBetweenAsString
import java.util.Collections
import java.util.Comparator

/**
 * Adapter for showing stations in a RecyclerView
 */
class StationAdapter(context: Context, val stationInteractionListener: StationInteractionListener) : RecyclerView.Adapter<StationAdapter.ViewHolder>() {

    var stations: List<Station> = mutableListOf()
    set(value) {
        field = value
        sortStations()
        notifyDataSetChanged()
        adapterDataObserver?.onStations(value)
    }
    var favorites: Set<Int> = setOf()
    var location: Location? = null
        set(value) {
            field = value
            sortStations()
            notifyDataSetChanged()
        }

    var adapterDataObserver: AdapterDataObserver? = null

    private val stationComparator = StationComparator()

    private val colorItemEnabled = ContextCompat.getColor(context, R.color.station_list_item_enabled)
    private val colorItemDisabled = ContextCompat.getColor(context, R.color.station_list_item_disabled)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_station_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val station = stations[position]
        holder.label.text = station.name
        holder.bikes.text = station.availableBikes.toString()
        holder.stands.text = station.availableBikeStands.toString()

        if (favorites.contains(station.id)) {
            holder.star.visibility = View.VISIBLE
        } else {
            holder.star.visibility = View.GONE
        }

        if (station.isOpen) {
            holder.itemView.isEnabled = true
            holder.itemView.setBackgroundColor(colorItemEnabled)
        } else {
            holder.itemView.isEnabled = false
            holder.itemView.setBackgroundColor(colorItemDisabled)
        }

        val location = this.location
        if (location != null) {
            holder.distance.text = getDistanceBetweenAsString(station, location)
            holder.distance.visibility = View.VISIBLE
        } else {
            holder.distance.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            stationInteractionListener.onStationClicked(station)
        }
    }

    override fun getItemCount(): Int {
        return stations.size
    }

    private fun sortStations() {
        if (location != null) {
            Collections.sort(stations, stationComparator)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        init {
            view.setOnCreateContextMenuListener(this)
        }
        val label: TextView = view.station_item_label
        val bikes: TextView = view.stationBikeText
        val stands: TextView = view.stationStandsText
        val distance: TextView = view.station_item_distance
        val star: ImageView = view.station_item_star

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.add(R.string.station_list_fragment_context_details)?.setOnMenuItemClickListener {
                stationInteractionListener.onContextMenuDetailsClicked(stations[adapterPosition])
                true
            }
            val toggleFavoriteItemTitleRes = if (star.visibility == View.VISIBLE)
                R.string.station_list_fragment_context_remove_favorite
                else R.string.station_list_fragment_context_add_favorite
            menu?.add(toggleFavoriteItemTitleRes)?.setOnMenuItemClickListener {
                stationInteractionListener.onContextMenuFavoriteToggled(stations[adapterPosition])
                true
            }
        }
    }

    private inner class StationComparator : Comparator<Station> {
        override fun compare(lhs: Station, rhs: Station): Int {
            val loc = location ?: return 0

            val result = FloatArray(1)
            Location.distanceBetween(lhs.lat, lhs.longitude, loc.latitude, loc.longitude, result)
            val lhsDist = result[0]
            Location.distanceBetween(rhs.lat, rhs.longitude, loc.latitude, loc.longitude, result)
            val rhsDist = result[0]

            return Math.round(lhsDist) - Math.round(rhsDist)
        }
    }

    interface StationInteractionListener {
        fun onStationClicked(station: Station)
        fun onContextMenuDetailsClicked(station: Station)
        fun onContextMenuFavoriteToggled(station: Station)
    }

    interface AdapterDataObserver {
        fun onStations(stations: List<Station>)
    }
}