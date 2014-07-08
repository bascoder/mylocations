package nl.basvanmarwijk.mylocations.viewcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

import nl.basvanmarwijk.mylocations.R;
import nl.basvanmarwijk.mylocations.db.dao.Location;

/**
 * An activity representing a list of Contents. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link LocationItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link LocationItemListFragment} and the item details (if present) is a
 * {@link LocationItemDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link LocationItemListFragment.Callbacks} interface to listen for item
 * selections.
 *
 * @version 1
 * @since revision 1
 */
public class LocationItemListActivity extends ActionBarActivity implements
        LocationItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * Callback method from {@link LocationItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Location item) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(LocationItemDetailFragment.ARG_ITEM_ID,
                    item.getId());

            LocationItemDetailFragment fragment = new LocationItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.locationitem_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this,
                    LocationItemDetailActivity.class);
            detailIntent.putExtra(LocationItemDetailFragment.ARG_ITEM_ID,
                    item.getId());
            startActivity(detailIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // support voor progressbar
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_locationitem_list);

        if (findViewById(R.id.locationitem_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((LocationItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.locationitem_list))
                    .setActivateOnItemClick(true);
        }

        // If exposing deep links into your app, handle intents here.
    }
}
