/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.oreore.hk.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 */
public class ImageFetcher extends ImageWorker {
    private static final String TAG = "ImageFetcher";

    /**
     * Initialize providing a target image width and height for the processing images.
     *
     * @param context
     */
    public ImageFetcher(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
    	;
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param imagePath The Path to load the bitmap
     * @return The bitmap
     */
    private Bitmap processBitmap(String imagePath) {
        Log.d(TAG, "processBitmap - " + imagePath);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        
        ImageCache cache = getImageCache();
        addInBitmapOptions(options, cache);
        return BitmapFactory.decodeFile(imagePath, options);
    }

    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                Log.d(TAG, "Found bitmap to use for inBitmap");
                options.inBitmap = inBitmap;
            }
        }
    }

}
