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
package se.geecity.android.steerandput.common.logging

/**
 * A favorite station is added
 *
 * Parameters:
 * {@link com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID}
 * {@link com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME}
 * {@link se.geecity.android.steerandput.common.logging.EventParams.SOURCE}
 */
const val ADD_FAVORITE = "add_favorite"

/**
 * A favorite station is removed
 *
 * Parameters:
 * {@link com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID}
 * {@link com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME}
 * {@link se.geecity.android.steerandput.common.logging.EventParams.SOURCE}
 */
const val REMOVE_FAVORITE = "remove_favorite"

/**
 * A page is created and shown to the user. Typically logged in Fragment.onViewCreated()
 *
 * Parameters:
 * {@link se.geecity.android.steerandput.common.logging.EventParams.VIEW_ID}
 */
const val PAGE_VIEW = "page_view"