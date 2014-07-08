package nl.basvanmarwijk.mylocations.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.io.Closeable;
import java.util.List;

/**
 * Handles location updates.
 * 
 * @author Bas van Marwijk
 * @since revision 1
 * @version 1.1
 * 
 */
public class LocationBridge implements Closeable {
	/**
	 * Inner class that listens for location updates
	 * 
	 * @author Bas
	 * 
	 */
	class MyLocationListener implements android.location.LocationListener {

		@Override
		public void onLocationChanged(Location newLocation) {
			if (lastLocation == null) {
				lastLocation = newLocation;
			} else if (age(lastLocation) > age(newLocation)) {
				lastLocation = newLocation;
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// ignore

		}

		@Override
		public void onProviderEnabled(String provider) {
			// ignore

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// ignore

		}

	}

	private static final long FIVE_MINS = 5 * 60 * 1000;

	private int minDistance = 1000;
	private Location lastLocation;
	private LocationManager locationManager;

	private LocationListener listener;

	public LocationBridge(Context context) {
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		listener = new MyLocationListener();
	}

	/**
	 * Stops listening
	 */
	@Override
	public void close() {
		this.stopListen();
	}

	public Location getLastLocation() throws LocationException {
		startListen();
		refreshLocation();
		return lastLocation;
	}

	/**
	 * Returns the age of a location in milliseconds
	 * 
	 * @param location
	 * @return age of location in milliseconds
	 */
	private long age(Location location) {
		return System.currentTimeMillis() - location.getTime();
	}

	private void refreshLocation() {
		List<String> providers = locationManager.getAllProviders();

		providers.remove(LocationManager.GPS_PROVIDER);

		Location mostAccuraat = null;
		for (final String p : providers) {
			Location l = locationManager.getLastKnownLocation(p);
			if (l != null) {
				if (mostAccuraat == null)
					mostAccuraat = l;
				else if (l.getAccuracy() > mostAccuraat.getAccuracy()
						&& age(l) <= FIVE_MINS) {
					mostAccuraat = l;
				}
			}
		}

		if (mostAccuraat != null) {
			if (age(mostAccuraat) <= FIVE_MINS) {
				lastLocation = mostAccuraat;
			}
		}
	}

	/**
	 * 
	 * @throws LocationException
	 *             als er geen providers zijn
	 */
	private void startListen() throws LocationException {
		try {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, FIVE_MINS, minDistance,
					listener);
		} catch (IllegalArgumentException e) {
			Criteria c = new Criteria();
			c.setAccuracy(Criteria.ACCURACY_LOW);

			String provider = locationManager.getBestProvider(c, true);
			if (provider == null) {
				throw new LocationException("No location provider available");
			} else {
				locationManager.requestLocationUpdates(provider, FIVE_MINS,
						minDistance, listener);
			}
		}
	}

	private void stopListen() {
		locationManager.removeUpdates(listener);
	}
}
