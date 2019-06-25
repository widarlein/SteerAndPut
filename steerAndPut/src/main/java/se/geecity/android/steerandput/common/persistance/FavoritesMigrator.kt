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
package se.geecity.android.steerandput.common.persistance

import android.content.Context

class FavoritesMigrator(private val context: Context, private val oldFavoriteUtil: OldFavoriteUtil, private val newFavoritesUtil: NewFavoritesUtil) {

    fun migrateIfPossible() {
        if (favoritesFileExists()) {
            val favorites = oldFavoriteUtil.favorites
            newFavoritesUtil.saveFavorites(favorites.map { it.id }.toSet())
            deleteOldFavoritesFile()
        }
    }

    fun favoritesFileExists() = context.fileList().any { it == OldFavoriteUtil.FILENAME }

    private fun deleteOldFavoritesFile() {
        context.deleteFile(OldFavoriteUtil.FILENAME)
    }
}