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
import android.database.DataSetObserver;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import nl.basvanmarwijk.mylocations.App;
import nl.basvanmarwijk.mylocations.R;
import nl.basvanmarwijk.mylocations.location.LocationBridge;
import nl.basvanmarwijk.mylocations.location.LocationException;
import nl.basvanmarwijk.mylocations.logic.PlaceDownloaderTask;

/**
 * A list fragment representing a list of Contents. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link LocationItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 *
 * TODO on item click gmaps api
 *
 * @author Bas
 * @since revision 1
 * @version 2.1 moved PlaceDownloaderTask.Callback in separate inner class
 * @version 2.0 uses {@link android.support.v4.widget.CursorAdapter}
 * @version 1.4 onResume refreshes the list adapter
 * @version 1.3 compatible with {@link PlaceDownloaderTask} version 2.1
 * @version 1.2 progress circle
 * @version 1.1 loads locationitems in de background
 * @version 1.0 creation
 */
public class LocationItemListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String TAG = LocationItemListFragment.class.getCanonicalName();

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = null;
    private CursorAdapter adapter;
    private AdapterDataListener adapterDataListener;
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LocationItemListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }

        adapter = new LocationItemCursorAdapter(App.getAppContext(),
                App.getDbManager().getAllColumnsCursor(),
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        adapterDataListener = new AdapterDataListener(this);
        adapter.registerDataSetObserver(adapterDataListener);

        mCallbacks = (Callbacks) activity;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @SuppressLint("InflateParams")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final View btn_add = getActivity().getLayoutInflater().inflate(
                R.layout.add_button, null);
        final ListView lv = getListView();
        lv.addHeaderView(btn_add);

        btn_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LocationBridge lb = new LocationBridge(
                        getActivity().getApplicationContext());

                final String TAG = ((Object) this).getClass().getName();
                Log.i(TAG, "start locatie zoeken");

                Location locatie = null;
                try {
                    locatie = lb.getLastLocation();
                } catch (LocationException e) {
                    Log.w(TAG, e.getMessage());
                } finally {
                    lb.close();
                }

                if (locatie != null) {
                    Toast.makeText(getActivity(), R.string.location_found,
                            Toast.LENGTH_SHORT).show();

                    toggleProgressBar(true);

                    PlaceDownloaderTask downloader = new PlaceDownloaderTask(
                            new PlaceDownloaderListener());
                    downloader.execute(locatie);

                } else {
                    Toast.makeText(getActivity(),
                            R.string.location_not_available, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        setListAdapter(adapter);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        refreshAdapter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = null;

        adapter.unregisterDataSetObserver(adapterDataListener);
    }

    /**
     * Refreshes list adapter
     */
    private void refreshAdapter() {
        Log.d(TAG, "Refreshing adapter");
        adapter.swapCursor(App.getDbManager().getAllColumnsCursor());
        adapter.notifyDataSetChanged();

        refreshListView();
    }

    private void refreshListView() {
        //adapter.swapCursor(App.getDbManager().getAllColumnsCursor());
        getListView().setAdapter(adapter);
    }

    /**
     * Sets progress bar to boolean enabled
     *
     * @param enabled enable the progressbar with true, disable with false
     */
    private void toggleProgressBar(boolean enabled) {
        getActivity().setProgressBarIndeterminateVisibility(enabled);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState
                    .getInt(STATE_ACTIVATED_POSITION));
        }

    }

    @Override
    public void onListItemClick(ListView listView, View view, int position,
                                long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        if (adapter.getCount() >= position) {
            mCallbacks.onItemSelected((nl.basvanmarwijk.mylocations.db.dao.Location) adapter
                    .getItem(position - 1));
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(
                activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
                        : ListView.CHOICE_MODE_NONE
        );
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(nl.basvanmarwijk.mylocations.db.dao.Location item);
    }

    private static class AdapterDataListener extends DataSetObserver {
        private WeakReference<LocationItemListFragment> mParent;

        private AdapterDataListener(LocationItemListFragment mParent) {
            this.mParent = new WeakReference<LocationItemListFragment>(mParent);
        }

        @Override
        public void onChanged() {
            super.onChanged();

            LocationItemListFragment parent = mParent.get();
            if (parent != null) {
                parent.refreshListView();
            }
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            //TODO implement
        }
    }

    private class PlaceDownloaderListener implements PlaceDownloaderTask.Callback {
        @Override
        public void onLoad(nl.basvanmarwijk.mylocations.db.dao.Location item) {

            // if no flag path is set, set the dummy picture
            if (item.getFlag_path() == null) {
                Uri dummyURI = Uri.parse("android.resource://"
                        + getActivity().getPackageName() + "/drawable/stub.jpg");
                item.setFlag_path(dummyURI.getPath());
            }
            toggleProgressBar(false);

            refreshAdapter();
        }

        @Override
        public void onProgress(byte progress) {
            // ignore
        }
    }
}
