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
package se.geecity.android.steerandput.common.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Station implements Parcelable, Serializable{
	

    @SerializedName("StationId")
	private int id;
	private String name;
	private double lat;
	@SerializedName("Long")
	private double longitude;
	private int bikeStands;
	private int availableBikes;
	private int availableBikeStands;
    private boolean isOpen;
	
	
	public Station(int id, String name, double lat, double longitude,
                   int bikeStands, int availableBikes, int availableBikeStands, String state, boolean isOpen) {
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.longitude = longitude;
		this.bikeStands = bikeStands;
		this.availableBikes = availableBikes;
		this.availableBikeStands = availableBikeStands;
        this.isOpen = isOpen;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public int getBikeStands() {
		return bikeStands;
	}
	public void setBikeStands(int bikeStands) {
		this.bikeStands = bikeStands;
	}
	public int getAvailableBikes() {
		return availableBikes;
	}
	public void setAvailableBikes(int availableBikes) {
		this.availableBikes = availableBikes;
	}
	public int getAvailableBikeStands() {
		return availableBikeStands;
	}
	public void setAvailableBikeStands(int availableBikeStands) {
		this.availableBikeStands = availableBikeStands;
	}

	public boolean isOpen() {
		return isOpen;
	}

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
	public String toString() {
		
		return "(" + getName() + " [" + getLat() + ","+getLongitude()+"] )";
	}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(name);
		out.writeDouble(lat);
		out.writeDouble(longitude);
		out.writeInt(bikeStands);
		out.writeInt(availableBikes);
		out.writeInt(availableBikeStands);
        out.writeInt((isOpen) ? 1 : 0 );
	}
	
	public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {

		public Station createFromParcel(Parcel source) {
			return new Station(source);
		}

		public Station[] newArray(int size) {
			return new Station[size];
		}
	};
	
	private Station(Parcel in) {
		id = in.readInt();
		name = in.readString();
		lat = in.readDouble();
		longitude = in.readDouble();
		bikeStands = in.readInt();
		availableBikes = in.readInt();
		availableBikeStands = in.readInt();
        isOpen = in.readInt() == 1;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		boolean same = false;

        if (o != null && o instanceof Station) {
            same = this.id == ((Station)o).id;
        }
        return same;
    }
}
