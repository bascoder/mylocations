/**
 *
 */
package nl.basvanmarwijk.mylocations.viewcontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.basvanmarwijk.mylocations.LocationItem;
import nl.basvanmarwijk.mylocations.R;

/**
 * The BaseAdapter of the main ListActivity.
 *
 * @author Bas van Marwijk
 * @version 1
 * @since revision 1
 * @deprecated since 6-7-2014 TODO replace with cursor adapter
 */
@Deprecated
public class LocationItemAdapter extends BaseAdapter {

    static class ItemView {
        ImageView flag;
        TextView country;
        TextView place;
    }

    List<LocationItem> itemList = new ArrayList<LocationItem>();
    private static LayoutInflater inflater = null;

    private Context context;

    public LocationItemAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    /**
     * Voegt item toe aan de adapter
     *
     * @param item om toe te voegen
     * @return true als het is toegevoegd anders false
     */
    public boolean add(LocationItem item) {

        boolean unique = true;
        for (final LocationItem i : itemList) {
            if (i.getPlace().equals(item.getPlace())) {
                unique = false;
                break;
            }
        }

        if (unique) {
            itemList.add(item);
            notifyDataSetChanged();
        }

        return unique;
    }

    public void clear() {
        itemList.clear();
    }

    /**
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return itemList.size();
    }

    /**
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    /**
     * (non-Javadoc)
     *
     * @return position
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View viewToCreate = convertView;
        ItemView holder;

        LocationItem item = itemList.get(position);

        if (convertView == null) {
            holder = new ItemView();
            viewToCreate = inflater.inflate(R.layout.placeview, null);

            try {
                holder.flag = (ImageView) viewToCreate
                        .findViewById(R.id.iv_flag);
                holder.country = (TextView) viewToCreate
                        .findViewById(R.id.tvCountry);
                holder.place = (TextView) viewToCreate
                        .findViewById(R.id.tvPlace);
                viewToCreate.setTag(holder);
            } catch (ClassCastException e) {
                Log.e(this.getClass().getName(),
                        "Could not cast all classes from View");
            }
        } else {
            holder = (ItemView) viewToCreate.getTag();
        }

        holder.flag.setImageBitmap(item.getFlag());
        holder.country.setText(context.getString(R.string.tv_country)
                + item.getCountry());
        holder.place.setText(context.getString(R.string.tv_place)
                + item.getPlace());

        return viewToCreate;
    }
}
