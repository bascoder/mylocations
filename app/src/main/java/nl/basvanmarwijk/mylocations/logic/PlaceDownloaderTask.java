package nl.basvanmarwijk.mylocations.logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.geonames.Toponym;
import org.geonames.WebService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import nl.basvanmarwijk.io.ExternalStorageHelper;
import nl.basvanmarwijk.mylocations.App;
import nl.basvanmarwijk.mylocations.LocationItem;
import nl.basvanmarwijk.mylocations.db.DBManager;
import nl.basvanmarwijk.mylocations.viewcontroller.LocationItemListFragment;

/**
 * Downloads location info from geonames.org and adds new location item to database
 * 
 * @author Bas van Marwijk - based on PlaceDownloaderTask of University of Marylands's mooc
 * @since revision 1
 * @version 3.1 returns dao's location
 * @version 3.0 uses dao to add to database
 * @version 2.4 fixed dependency with context and memory leak
 * @version 2.3 handles exceptions thrown by file io
 * @version 2.2 switched progress update to Byte, progress doesn't count higher
 *          than 100
 * @version 2.1 Added {@link Callback} interface for generic access, broke dependency
 *          with viewcontroller
 * @version 2.0 28-04-2014, switch to Geonames Java library API
 * @version 1.1 call to different method in {@link LocationItemListFragment}
 * @version 1.0 creation, uses geonames via webapi
 * 
 */
public class PlaceDownloaderTask extends
		AsyncTask<Location, Byte, nl.basvanmarwijk.mylocations.db.dao.Location> {

	/**
	 * Callback mechanism for this class
	 * 
	 * @author Bas van Marwijk
	 * @since 28-04-2014, version 2.1
	 * @version 1.0 creation
	 */
	public interface Callback {
		/**
		 * Called on load completation
		 * 
		 * @param loadedItem
		 *            item that has been downloaded
		 */
		void onLoad(nl.basvanmarwijk.mylocations.db.dao.Location loadedItem);

		/**
		 * Called on progress update
		 * 
		 * @param progress
		 *            progress in byteint
		 */
		void onProgress(byte progress);
	}

	// geonames.org account name for API access
	private static String USERNAME = "bas1994";
	private static String TAG = PlaceDownloaderTask.class.getCanonicalName();

	private Uri generateFlagURL(String countryCode) {
		return Uri.parse("http://www.geonames.org/flags/x/" + countryCode
				+ ".gif");
	}

	private HttpURLConnection mHttpUrl;

	private WeakReference<Callback> mParent;
	private Location mLocation;

	public PlaceDownloaderTask(Callback parent) {
		super();
		mParent = new WeakReference<Callback>(parent);
	}

	@Override
	protected nl.basvanmarwijk.mylocations.db.dao.Location doInBackground(Location... location) {

		nl.basvanmarwijk.mylocations.db.dao.Location place;
		mLocation = location[0];

		place = getPlaceFromApi();

		// half klaar
		publishProgress((byte) 30);

		if (!place.getCountry().isEmpty()) {
			// bijna klaar
			publishProgress((byte) 60);

			Uri newPath = null;
			try {
				newPath = ExternalStorageHelper.storeBitmap(App.getAppContext(),
						getFlagFromURL(Uri.parse(place.getFlag_path())), place.getCountry());
			} catch (IllegalStateException e) {
				Log.w(TAG, e.getLocalizedMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
			place.setFlag_path(newPath.getPath());

            addToDatabase(place);
		} else {
			place = null;
		}

		publishProgress((byte) 90);

		return place;

	}

	@Override
	protected void onPostExecute(nl.basvanmarwijk.mylocations.db.dao.Location result) {

		if (null != result && null != mParent.get()) {
			mParent.get().onLoad(result);
		}
	}

	@Override
	protected void onProgressUpdate(Byte... values) {

	}

    private void addToDatabase(nl.basvanmarwijk.mylocations.db.dao.Location item){
        DBManager dbManager = App.getDbManager();
        dbManager.insertLocation(item, true);
    }

	private Bitmap getFlagFromURL(Uri flagUrl) {

		InputStream in = null;

		Log.i("temp", flagUrl.toString());

		try {
			URL url = new URL(flagUrl.toString());
			mHttpUrl = (HttpURLConnection) url.openConnection();
			in = mHttpUrl.getInputStream();

			return BitmapFactory.decodeStream(in);

		} catch (MalformedURLException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mHttpUrl.disconnect();
		}

		return null;
	}

	/**
	 * Get LocationItem from Geonames library API
	 * 
	 * @since version 2.0
	 * @return LocationItem filled with data
	 */
	private nl.basvanmarwijk.mylocations.db.dao.Location getPlaceFromApi() {
		// login
		WebService.setUserName(USERNAME);

		// nieuw object met bekende locatie
		LocationItem item = new LocationItem();
		item.setLocation(mLocation);

		// stop data in object, return null by errors
		try {
			List<Toponym> plaatsen = WebService.findNearbyPlaceName(
					mLocation.getLatitude(), mLocation.getLongitude());

			if (plaatsen.isEmpty())
				return null;
			else {
				final Toponym plaats = plaatsen.get(0);
				item.setCountry(plaats.getCountryName());
				item.setPlace(plaats.getName());

				Uri flagUri = generateFlagURL(plaats.getCountryCode()
						.toLowerCase(Locale.US));
				item.setFlagPath(flagUri);
			}
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(),
					"Error during getting data from geonames api");
			return null;
		} catch (Exception e) {
			Log.e(getClass().getCanonicalName(),
					"Unknown exception thrown by geonames api\nprinting stacktrace");
			e.printStackTrace();
			return null;
		}

		return item.toLocation(false);
	}

}
