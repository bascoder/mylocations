/*
 * Copyright 2014 Bas van Marwijk
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

package nl.basvanmarwijk.mylocations.viewcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nl.basvanmarwijk.io.ExternalStorageHelper;
import nl.basvanmarwijk.io.ImageHelper;
import nl.basvanmarwijk.mylocations.App;
import nl.basvanmarwijk.mylocations.R;
import nl.basvanmarwijk.mylocations.db.DBManager;
import nl.basvanmarwijk.mylocations.db.dao.Location;
import nl.basvanmarwijk.mylocations.db.dao.Location_picture;

/**
 * A fragment representing a single LocationItem detail screen. This fragment is
 * either contained in a {@link LocationItemListActivity} in two-pane mode (on
 * tablets) or a {@link LocationItemDetailActivity} on handsets.
 * TODO threads management
 * TODO use locks everywhere
 * TODO get rid of deprecated LocationItem
 *
 * @author Bas
 * @since revision 1
 * @version 2.0 (partly) compatible with green dao,
 *      introduced use of {@link java.util.concurrent.locks.ReentrantReadWriteLock},
 *      fixed update of imageview
 * @version 1.9 added extra exception handling for file io
 * @version 1.8 confirmation dialog when deleting item
 * @version 1.7 doesn't delete new picture by accident
 * @version 1.6 progress circle
 * @version 1.5 bitmap scales and loads in separate thread
 * @version 1.4 storage and db tasks in separate threads
 * @version 1.3 add picture view
 * @version 1.2 add picture to location item
 * @version 1.1 remove item
 * @version 1.0 creation
 */
public class LocationItemDetailFragment extends Fragment {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private static final String ARG_CAMERA_URI = "camera_uri";
    private static final byte CAMERA_INTENT_REQUEST_CODE = 50;
    private static final String TAG = LocationItemDetailFragment.class
            .getCanonicalName();

    private ReentrantReadWriteLock.ReadLock itemReadLock;
    private ReentrantReadWriteLock.WriteLock itemWriteLock;

    private volatile Location mItem;
    private Uri pictureUri;
    private View rootView;
    private LinearLayout progressBar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LocationItemDetailFragment() {

    }

    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_INTENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                updateCameraResult();
                Log.i(getTag(), pictureUri.getPath());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ReentrantReadWriteLock itemLock = new ReentrantReadWriteLock();
        itemReadLock = itemLock.readLock();
        itemWriteLock = itemLock.writeLock();

        // verkrijg de locatie item uit de database
        final Bundle args = getArguments();
        if (args.containsKey(ARG_ITEM_ID)) {
            final long id = args.getLong(ARG_ITEM_ID);

            DBManager dbManager = App.getDbManager();
            Location item = dbManager.getLocationById(id);
            mItem = item;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_locationitem_detail,
                container, false);

        if (mItem != null) {
            // voeg de placeview toe
            FrameLayout rootLayout = (FrameLayout) getActivity().findViewById(
                    android.R.id.content);
            View.inflate(getActivity(), R.layout.placeview, rootLayout);

            TextView tvCountry = (TextView) rootView
                    .findViewById(R.id.tvCountry);
            TextView tvPlace = (TextView) rootView.findViewById(R.id.tvPlace);
            ImageView imgView = (ImageView) rootView.findViewById(R.id.iv_flag);

            tvCountry.setText(tvCountry.getText() + mItem.getCountry());
            tvPlace.setText(tvPlace.getText() + mItem.getPlace());
            imgView.setImageBitmap(
                    ExternalStorageHelper.readBitmap(
                            Uri.parse(mItem.getFlag_path()))
            );

            // verander de action bar title
            String title = String.format(Locale.getDefault(), "%s: %s, %s",
                    getString(R.string.title_locationitem_detail),
                    mItem.getPlace(), mItem.getCountry());
            getActivity().setTitle(title);

            // vind progressbar
            View inflated = View.inflate(getActivity(),
                    R.layout.progress_circle, rootLayout);
            if (inflated == null) {
                Log.e(getTag(), "inflated include is nulll");
            }
            progressBar = (LinearLayout) getActivity().findViewById(
                    R.id.ll_progress_circle);
            if (progressBar == null) {
                Log.e(getTag(), "progressbaar is null");
            }

            Button addPicture = (Button) rootView
                    .findViewById(R.id.btn_add_picture);
            addPicture.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    launchCamera();
                }
            });

            Button removeItem = (Button) rootView
                    .findViewById(R.id.btn_remove_content);
            removeItem.setOnClickListener(new OnClickListener() {

                // build a dialog that askes for confirmation
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Titel")
                            .setMessage(R.string.alert_remove_are_you_sure)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                            if (whichButton == AlertDialog.BUTTON_POSITIVE) {
                                                removeCurrentItem();
                                            }
                                        }
                                    }
                            )
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            });
        }

        updateImageView();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // restore pictureUri
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ARG_CAMERA_URI)) {
                pictureUri = Uri.parse(savedInstanceState
                        .getString(ARG_CAMERA_URI));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // store uri if it gets destroyed
        if (pictureUri != null) {
            outState.putString(ARG_CAMERA_URI, pictureUri.toString());
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            pictureUri = ExternalStorageHelper
                    .createUriForNewMediaFile();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);

            startActivityForResult(intent, CAMERA_INTENT_REQUEST_CODE);
        } catch (IOException e) {
            Toast.makeText(getActivity(),
                    R.string.toast_could_not_store_picture, Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Verwijdert item en verlaat activity
     */
    private void removeCurrentItem() {
        // doe werk buiten UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                // delete van db_depecrated
                try {
                    App.getDbManager().deleteLocation(mItem);
                } catch (SQLException e) {
                    Log.w(TAG, e.getLocalizedMessage());
                }

                // delete van sd kaart
                if (!mItem.getFk_location_picture().isEmpty()) {
                    try {
                        for (final Location_picture pic : mItem.getFk_location_picture()) {
                            ExternalStorageHelper.removeFileFromUri(
                                    Uri.parse(pic.getPicture_path()));
                        }
                    } catch (IllegalStateException e) {
                        getActivity().runOnUiThread(
                                new ToastRunnable(R.string.sd_card_in_use));
                        Log.w(TAG, "sd in use");
                    } catch (IOException e) {
                        Log.e(TAG, "Could not remove item from storage");
                    }
                }

                // release resources
                mItem = null;
            }
        }).start();

        // quit activity
        getActivity().finish();
    }

    private void updateCameraResult() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // synchronize mItem
                    itemWriteLock.lock();

                    DBManager db = App.getDbManager();
                    Location_picture lp = new Location_picture();
                    lp.setLocation_id(mItem.getId());
                    lp.setPicture_path(pictureUri.getPath());
                    db.getLocationPictureDao().insert(lp);

                    mItem.resetFk_location_picture();

                } catch (Exception e) {
                    getActivity().runOnUiThread(
                            new ToastRunnable(
                                    R.string.toast_could_not_store_picture)
                    );
                    // clean failed picture
                    try {
                        ExternalStorageHelper.removeFileFromUri(pictureUri);
                    } catch (Exception e2) { // IOException |
                        // IllegalStateException
                        Log.w(TAG, "Could not clean failed picture");
                    }
                } finally {
                    // unlock
                    itemWriteLock.unlock();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                updateImageView();
            }

        }.execute();
    }

    /**
     * Gets Bitmap from mItem and updates into the ImageView
     */
    private void updateImageView() {

        if (mItem != null) {
            final Uri picLocation;
            try {
                itemReadLock.lock();

                final List<Location_picture> pictures = mItem.getFk_location_picture();
                if (pictures.isEmpty()) {
                    return;
                    }
                picLocation = Uri.parse(
                        pictures.get(
                                pictures.size() - 1)
                                .getPicture_path()
                );
            } finally {
                itemReadLock.unlock();
            }

            if (picLocation != null) {
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected void onPreExecute() {
                        // progress bar on
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Bitmap doInBackground(Void... params) {

                        Bitmap b = null;
                        try {
                            // get bitmap
                            b = ExternalStorageHelper.readBitmap(picLocation);
                            // get preferred width
                            int width = ImageHelper
                                    .getScreenDimension(getActivity()).x;
                            // scale bitmap
                            b = ImageHelper.scaleBitmap(b, width);
                        } catch (IllegalStateException e) {
                            getActivity().runOnUiThread(
                                    new ToastRunnable(R.string.sd_card_in_use));
                        } catch(IllegalArgumentException e) {
                            //ignore
                            //TODO remove corrupt uri from db
                        }

                        return b;
                    }

                    @Override
                    protected void onPostExecute(Bitmap result) {
                        ImageView iv = (ImageView) rootView
                                .findViewById(R.id.iv_photoView);

                        iv.setImageBitmap(result);
                        iv.setVisibility(View.VISIBLE);

                        // progress bar off
                        progressBar.setVisibility(View.GONE);
                    }
                }.execute();

            }
            }
    }

    /**
     * Runs specified toast message
     *
     * @author Bas
     * @version 1.1 performs check so it always runs on the ui thread
     */
    private class ToastRunnable implements Runnable {

        final Toast toast;

        @SuppressLint("ShowToast")
        public ToastRunnable(String message) {
            toast = Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
        }

        @SuppressLint("ShowToast")
        public ToastRunnable(int messageResId) {
            toast = Toast.makeText(getActivity(), messageResId,
                    Toast.LENGTH_LONG);
        }

        @Override
        public void run() {
            // runs itself in UI thread
            if (inUiThread())
                toast.show();
            else
                runOnUiThread();
        }

        /**
         * TODO test
         *
         * @return true if this methods is called from UI thread
         */
        private boolean inUiThread() {
            return Looper.myLooper() == Looper.getMainLooper();
        }

        /**
         * Runs itself on ui thread
         */
        public void runOnUiThread() {
            getActivity().runOnUiThread(this);
        }
    }
}
