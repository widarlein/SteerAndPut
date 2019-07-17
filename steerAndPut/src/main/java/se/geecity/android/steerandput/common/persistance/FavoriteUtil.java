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

import java.util.List;
import java.util.Set;

import se.geecity.android.steerandput.common.exception.SteerAndPutException;
import se.geecity.android.steerandput.common.model.Station;

public class FavoriteUtil {

    private static String TAG = FavoriteUtil.class.getSimpleName();

    private final NewFavoritesUtil mNewFavoritesUtil;

    public FavoriteUtil(Context context) {
        OldFavoriteUtil oldFavoriteUtil = new OldFavoriteUtil(context);
        mNewFavoritesUtil = new NewFavoritesUtil(context);
        FavoritesMigrator favoritesMigrator = new FavoritesMigrator(context, oldFavoriteUtil, mNewFavoritesUtil);
        favoritesMigrator.migrateIfPossible();
    }


    public Set<Integer> getFavorites() {
        return mNewFavoritesUtil.getFavorites();
    }

    public void addFavorite(int stationId) {
        mNewFavoritesUtil.addFavorite(stationId);
    }

    public void removeFavorite(int stationId) {
        mNewFavoritesUtil.removeFavorite(stationId);
    }

}
