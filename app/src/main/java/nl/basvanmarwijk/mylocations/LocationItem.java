package nl.basvanmarwijk.mylocations;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

import nl.basvanmarwijk.io.ExternalStorageHelper;
import nl.basvanmarwijk.mylocations.db.dao.Location;

/**
 * 
 * @author Bas van Marwijk
 * @deprecated since 6-7-2014 TODO replace with dao interface
 * 
 */
@Deprecated
public class LocationItem {

	private long id;
	private double longitude;
	private double altitude;
	private double latitude;
	private String place;
	private String country;
	private Uri flagPath;
	@Deprecated
	private Uri picturePath;
	private Uri lastPicture;
	private Set<Uri> pictures = new HashSet<Uri>();
	private Set<Long> timestamps = new HashSet<Long>();

	public LocationItem() {

	}

	public void addPicture(final Uri picture) {
		synchronized (pictures) {
			pictures.add(picture);
			lastPicture = picture;
		}
	}

    public Location toLocation(boolean setId){
        final Location l = new Location();
        if(setId){
            l.setId(id);
        }
        l.setLongitude(longitude);
        l.setLatitude(latitude);
        l.setAltitude(altitude);
        l.setPlace(place);
        l.setCountry(country);
        l.setFlag_path(flagPath.toString());
        return l;
    }

    public static LocationItem fromLocation(final Location item){
        final LocationItem l = new LocationItem();
        l.setCountry(item.getCountry());
        l.setPlace(item.getPlace());
        l.setLongitude(item.getLongitude());
        l.setLatitude(item.getLatitude());
        l.setAltitude(item.getAltitude());
        try {
            l.setFlagPath(Uri.parse(item.getFlag_path()));
        } catch(Exception e){
            // ignore
        }
        return l;
    }

	public Uri getLastPicture() {
		return lastPicture;
	}

	public Uri[] getAllPictures() {
		synchronized (pictures) {
			return (Uri[]) pictures.toArray();
		}
	}
	
	public void setAllPictures(final Set<Uri> items){
		synchronized (pictures) {
			pictures = items;
		}
	}
	
	public void setAllPictures(final Set<Uri> items, Uri lastItem){
		synchronized (pictures) {
			pictures = items;
			lastPicture = lastItem;
		}
	}
	
	public void addTimestamp(final long timestamp) {
		synchronized (timestamps) {
			timestamps.add(timestamp);
		}
	}

	public Long[] getAllTimestamps() {
		synchronized (timestamps) {
			return (Long[]) timestamps.toArray();
		}
	}
	
	public void setAllTimestamps(Set<Long> items){
		synchronized (timestamps) {
			timestamps = items;
		}
	}


	public LocationItem(int id, android.location.Location location, String place,
			String country, Uri flagPath, Uri picturePath) {
		this.id = id;
		setLocation(location);
		this.place = place;
		this.country = country;
		this.flagPath = flagPath;
		this.picturePath = picturePath;
	}

	public LocationItem(android.location.Location location, String place, String country,
			Uri flagPath, Uri picturePath) {
		setLocation(location);
		this.place = place;
		this.country = country;
		this.flagPath = flagPath;
		this.picturePath = picturePath;
	}

	public String getCountry() {
		return country;
	}

	public Bitmap getFlag() {
		return ExternalStorageHelper.readBitmap(flagPath);
	}

	public Uri getFlagPath() {
		return flagPath;
	}

	public long getId() {
		return id;
	}

	public Uri getPicturePath() {
		return picturePath;
	}

	public String getPlace() {
		return place;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setFlagPath(Uri flagPath) {
		this.flagPath = flagPath;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Sets latitude, longtitude and altitude from Location object
	 * 
	 * @param location
	 */
	public void setLocation(android.location.Location location) {
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
		this.altitude = location.getAltitude();
	}

	public void setPicturePath(Uri picturePath) {
		this.picturePath = picturePath;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
