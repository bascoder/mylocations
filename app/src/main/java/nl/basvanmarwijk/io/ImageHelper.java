/*
 * Copyright ($year) Bas van Marwijk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basvanmarwijk.io;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

/**
 * Helps with images
 *
 * @author Bas
 * @version 1 creation, methods for bitmap scaling and retrieving screen size
 * @since 27-04-2014
 */
public class ImageHelper {

    /**
     * Returns scaled bitmap
     *
     * @param in        bitmap to scale
     * @param newHeight new height to scale to
     * @param newWidth  new width to scale to
     * @return new scaled bitmap
     */
    public static Bitmap scaleBitmap(final Bitmap in, int newHeight,
                                     int newWidth) {
        float curWidth = in.getWidth();
        float curHeight = in.getHeight();

        // scales
        float scaleWidth = newWidth / curWidth;
        float scaleHeight = newHeight / curHeight;

        // jeet.chanchawat from stackoverflow:
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap scaledBitmap = Bitmap.createBitmap(in, 0, 0, (int) curWidth,
                (int) curHeight, matrix, false);
        return scaledBitmap;
    }

    /**
     * Returns a scaled bitmap, height gets scaled with the newWidth.
     *
     * @param in       , de bitmap om te scalen
     * @param newWidth de nieuwe breedte
     * @return een bitmap met newWidth als breedte, hoogte wordt automatisch
     * meegeschaald
     */
    public static Bitmap scaleBitmap(final Bitmap in, int newWidth) {
        double curWidth = in.getWidth();
        double curHeight = in.getHeight();

        // calc nieuwe height met de schaal
        double scale = curWidth / newWidth;
        double newHeight = curHeight / scale;
        return scaleBitmap(in, (int) newHeight, newWidth);
    }

    /**
     * Returns screen dimensions in a point, works for all android APIs
     *
     * @param context
     * @return Point with x as width and y as height
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Point getScreenDimension(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }

        return size;
    }
}
