/*
 * Copyright (2014) Bas van Marwijk
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.basvanmarwijk.mylocations.App;

/**
 * Class with static io helper methods
 *
 * @author Bas van Marwijk
 * @version 2.1 - removed context parameters parameters, coupled with {@link nl.basvanmarwijk.mylocations.App}
 * @version 2.0 added proper exception handling, removed useless synchronization
 * @version 1.6 read/write actions synchronized
 * @version 1.5 storeBitmap long method refactor
 * @version 1.4 fixed bug where {@link #storeBitmap(android.graphics.Bitmap, String)} returns null if file already exists
 * @version 1.3 extensions
 * @version 1.2 null checks
 * @version 1.1 support voor mediafiles toevoegen en verwijderen
 * @version 1.0 creation
 * @since revision 1
 */
public final class ExternalStorageHelper {
    private final static Class<ExternalStorageHelper> mClass = ExternalStorageHelper.class;
    private final static String TAG = mClass.getCanonicalName();
    private final static String E_SD_NOT_WRITEABLE = TAG
            + ": SD storage is not writable";
    private final static String E_SD_NOT_MOUNTED = TAG
            + " :External media not mounted";

    private final static String PNG_EXTENSION = ".png";
    private final static String JPG_EXTENSION = ".jpg";

    private ExternalStorageHelper() {
    }

    /**
     * Creates Uri for media file in SD storage
     *
     * @return new Uri pointing to file with following format:
     * yyyy-MM-dd__HH_mm_ss
     * @throws IOException           if file creation failed
     * @throws IllegalStateException if sd not writable
     */
    public static Uri createUriForNewMediaFile()
            throws IOException, IllegalStateException {

        if (isWritable()) {
            final String DATE_FORMAT = "yyyy-MM-dd__HH_mm_ss";

            String timeStamp = new SimpleDateFormat(DATE_FORMAT, Locale.US)
                    .format(new Date());

            final Context context = App.getAppContext();

            // voeg een bestand toe met jpg extensie
            File file = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    timeStamp + JPG_EXTENSION);

            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IOException(mClass.getName()
                        + ": Could not create file");
            }
            if (!file.exists()) {
                throw new IOException(mClass.getName()
                        + ": Could not create file");
            }
            return Uri.fromFile(file);

        } else
            throw new IllegalStateException(E_SD_NOT_WRITEABLE);
    }

    private static boolean isWritable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * Reads image from Uri to Bitmap.
     *
     * @param filePath reads bitmap from filePath
     * @return bitmap containing the picture
     * @throws IllegalStateException          if SD storage not mounted
     * @throws IllegalArgumentException       if Uri pointing to invalid file
     * @throws java.lang.NullPointerException
     */
    public static Bitmap readBitmap(final Uri filePath)
            throws IllegalArgumentException, IllegalStateException {

        if (filePath == null) {
            throw new NullPointerException("Uri is empty");
        }
        Bitmap bmp = null;

        synchronized (filePath.getPath()) {

            if (isReadable()) {
                bmp = BitmapFactory.decodeFile(filePath.getPath());
            } else {
                throw new IllegalStateException(E_SD_NOT_MOUNTED);
            }
        }

        if (bmp == null) {
            throw new IllegalArgumentException(
                    "Uri is not pointing to an image file");
        }
        return bmp;

    }

    private static boolean isReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Removes file in the given Uri
     *
     * @param toRemove Uri pointing to the file to remove
     * @throws FileNotFoundException if Uri points to invalid file
     * @throws IOException           if removal failed
     * @throws IllegalStateException if SD not writable
     */
    public static void removeFileFromUri(Uri toRemove)
            throws IOException, IllegalStateException {
        if (isWritable() && toRemove != null) {
            File bestand = new File(toRemove.getPath());
            boolean success = removeFile(bestand);

            if (!success) {
                if (!bestand.exists()) {
                    throw new FileNotFoundException("Couldn't find: " + toRemove.getPath());
                } else {
                    throw new IOException("Couldn't remove following file: " + toRemove.getPath());
                }
            }
        } else if (!isWritable()) {
            throw new IllegalStateException(E_SD_NOT_WRITEABLE);
        }
    }

    private static boolean removeFile(final File file) {
        return file.delete();
    }

    /**
     * @throws IOException
     * @throws IllegalStateException if SD not writable
     * @see {@link #storeBitmap(android.graphics.Bitmap, String, boolean)}
     */
    public static Uri storeBitmap(Bitmap bmp, String name)
            throws IllegalStateException, IOException {
        return storeBitmap(bmp, name, false);
    }

    /**
     * Stores bitmap to SD storage, returns Uri of the new storage location
     *
     * @param bmp               the bitmap to store
     * @param name              filename
     * @param overwriteOnExists overwrites file with the same name, with false the Uri to the
     *                          double file will be returned
     * @return Uri of new location
     * @throws IllegalArgumentException SD not writable
     * @throws IOException
     * @throws IllegalStateException
     */
    private static Uri storeBitmap(Bitmap bmp,
                                   String name, boolean overwriteOnExists)
            throws IllegalStateException, IOException {
        if (isWritable()) {
            final Context context = App.getAppContext();

            // krijg file object
            final File dir = context
                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(dir, name + PNG_EXTENSION);

            boolean created;
            try {
                created = file.createNewFile();
            } catch (IOException e) {
                throw e;
            }

            // als overwrite enabled is, vervang het bestand dan
            if (!created && overwriteOnExists) {
                removeFile(file);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    // hoort niet voor te komen
                    Log.e(TAG, "code kan hier niet komen");
                    e.printStackTrace();
                    throw new Error(e);
                }

            }
            writeFileFromBitmap(file, bmp);

            return Uri.parse(file.getAbsolutePath());
        } else {
            throw new IllegalStateException(E_SD_NOT_WRITEABLE);
        }
    }

    private static Uri writeFileFromBitmap(final File file, final Bitmap bmp)
            throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 0, os);

            // success
            Log.i(mClass.getName(), "Saved: " + file.getAbsolutePath());
            return Uri.parse(file.getAbsolutePath());
        } catch (IOException e) {
            throw e;
        } finally {
            closeOutputStream(os);
        }
    }

    private static void closeOutputStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                Log.w(TAG, "Can't close stream");
            }
        }
    }
}
