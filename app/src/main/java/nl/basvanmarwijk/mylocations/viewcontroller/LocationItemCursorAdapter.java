package nl.basvanmarwijk.mylocations.viewcontroller;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import nl.basvanmarwijk.io.ExternalStorageHelper;
import nl.basvanmarwijk.mylocations.App;
import nl.basvanmarwijk.mylocations.R;
import nl.basvanmarwijk.mylocations.db.dao.LocationDao;

/**
 * @author Bas van Marwijk
 * @version 1.0 - creation
 * @since 6-7-2014
 */
public class LocationItemCursorAdapter extends CursorAdapter {

    private static final String TAG = LocationItemCursorAdapter.class.getSimpleName();

    /**
     * Holder holds the content of the view to create
     */
    private static class ItemView {
        ImageView flag;
        TextView country;
        TextView place;
    }

    private LayoutInflater inflater = null;

    /**
     * Constructor that allows control over auto-requery.  It is recommended
     * you not use this, but instead java.lang.String{@link #CursorAdapter(android.content.Context, android.database.Cursor, int)}.
     * When using this constructor, {@link #FLAG_REGISTER_CONTENT_OBSERVER}
     * will always be set.
     *
     * @param context     The context
     * @param c           The cursor from which to get the data.
     * @param autoRequery If true the adapter will call requery() on the
     *                    cursor whenever it changes so the most recent
     */
    public LocationItemCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);

        inflater = LayoutInflater.from(context);
    }

    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter; may
     *                be any combination of {@link #FLAG_AUTO_REQUERY} and
     *                {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public LocationItemCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        inflater = LayoutInflater.from(context);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View newView;
        ItemView holder = new ItemView();

        //haal de view op uit xml
        newView = inflater.inflate(R.layout.placeview, parent, false);
        try {
            holder.flag = (ImageView) newView.findViewById(R.id.iv_flag);
            holder.country = (TextView) newView.findViewById(R.id.tvCountry);
            holder.place = (TextView) newView.findViewById(R.id.tvPlace);

            newView.setTag(holder);
        } catch (ClassCastException e) {
            Log.e(TAG, "Could not create view from inflater: " + e.getMessage());
        }

        return newView;
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        try {
            ItemView holder = (ItemView) view.getTag();
            bindData(cursor, holder, context);
        } catch (ClassCastException e) {
            Log.e(TAG, "Passed view contains invalid tag");
        }
    }

    /**
     * @param position
     * @see android.widget.ListAdapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        long id = this.getItemId(position);
        return App.getDbManager().getLocationById(id);
    }

    private void bindData(final Cursor c, final ItemView holder, Context context) {
        String country = c.getString(LocationDao.Properties.Country.ordinal);
        String place = c.getString(LocationDao.Properties.Place.ordinal);
        String flagUri = c.getString(LocationDao.Properties.Flag_path.ordinal);

        Bitmap bmp = ExternalStorageHelper.readBitmap(Uri.parse(flagUri));

        holder.place.setText(context.getResources().getString(R.string.tv_place)
                + place);
        holder.country.setText(context.getResources().getString(R.string.tv_country)
                + country);
        holder.flag.setImageBitmap(bmp);
    }
}
