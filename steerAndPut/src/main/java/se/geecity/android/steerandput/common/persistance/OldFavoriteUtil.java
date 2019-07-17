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
package se.geecity.android.steerandput.common.persistance;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import se.geecity.android.steerandput.common.exception.SteerAndPutException;
import se.geecity.android.steerandput.common.model.Station;

public class OldFavoriteUtil {

    private static String TAG = OldFavoriteUtil.class.getSimpleName();

    static String FILENAME = "USER_FAVORITES2";

    private final Context mContext;

    public OldFavoriteUtil(Context context) {
        mContext = context;
    }


    public List<Station> getFavorites() {

        ObjectInputStream inputStream = null;
        List<Station> favorites = null;
        try {
            inputStream = new ObjectInputStream(new BufferedInputStream(mContext.openFileInput(FILENAME)));
            if (inputStream != null) {
                favorites = (List<Station>) inputStream.readObject();

            }

        } catch (FileNotFoundException e) {
            return init();
        } catch (IOException e) {
            Log.e(TAG, "Input stream error", e);
            throw new SteerAndPutException();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Input stream error", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't close stream", e);
            }
        }

        return favorites;
    }

    public void saveFavorites(List<Station> favorites) throws SteerAndPutException {
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new BufferedOutputStream(mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE)));
            outputStream.writeObject(favorites);

        } catch (IOException e) {
            Log.e(TAG, "Output stream error", e);
            throw new SteerAndPutException();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Couldn't close stream", e);
            }
        }
    }

    public List<Station> init() {
        List<Station> stations = new ArrayList<Station>();
        saveFavorites(stations);
        return stations;
    }

    public void addFavorite(Station favorite) {
        List<Station> favorites = getFavorites();
        favorites.add(favorite);
        saveFavorites(favorites);
    }

    public void removeFavorite(Station favorite) {
        List<Station> favorites = getFavorites();
        favorites.remove(favorite);
        saveFavorites(favorites);
    }

    public boolean isInitialized() {
        boolean result = false;
        try {
            InputStream inputStream = mContext.openFileInput(FILENAME);
            if (inputStream != null) {
                result = true;
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found. Not initialized");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't close stream", e);
        }

        return result;
    }


}
