package nl.basvanmarwijk.mylocations.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import nl.basvanmarwijk.mylocations.db.dao.Location;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table LOCATION.
*/
public class LocationDao extends AbstractDao<Location, Long> {

    public static final String TABLENAME = "LOCATION";

    /**
     * Properties of entity Location.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property Flag_path = new Property(1, String.class, "flag_path", false, "FLAG_PATH");
        public final static Property Country = new Property(2, String.class, "country", false, "COUNTRY");
        public final static Property Place = new Property(3, String.class, "place", false, "PLACE");
        public final static Property Longitude = new Property(4, Double.class, "longitude", false, "LONGITUDE");
        public final static Property Latitude = new Property(5, Double.class, "latitude", false, "LATITUDE");
        public final static Property Altitude = new Property(6, Double.class, "altitude", false, "ALTITUDE");
    };

    private DaoSession daoSession;


    public LocationDao(DaoConfig config) {
        super(config);
    }
    
    public LocationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'LOCATION' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," + // 0: id
                "'FLAG_PATH' TEXT," + // 1: flag_path
                "'COUNTRY' TEXT," + // 2: country
                "'PLACE' TEXT," + // 3: place
                "'LONGITUDE' REAL," + // 4: longitude
                "'LATITUDE' REAL," + // 5: latitude
                "'ALTITUDE' REAL);"); // 6: altitude
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'LOCATION'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Location entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String flag_path = entity.getFlag_path();
        if (flag_path != null) {
            stmt.bindString(2, flag_path);
        }
 
        String country = entity.getCountry();
        if (country != null) {
            stmt.bindString(3, country);
        }
 
        String place = entity.getPlace();
        if (place != null) {
            stmt.bindString(4, place);
        }
 
        Double longitude = entity.getLongitude();
        if (longitude != null) {
            stmt.bindDouble(5, longitude);
        }
 
        Double latitude = entity.getLatitude();
        if (latitude != null) {
            stmt.bindDouble(6, latitude);
        }
 
        Double altitude = entity.getAltitude();
        if (altitude != null) {
            stmt.bindDouble(7, altitude);
        }
    }

    @Override
    protected void attachEntity(Location entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Location readEntity(Cursor cursor, int offset) {
        Location entity = new Location( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // flag_path
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // country
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // place
            cursor.isNull(offset + 4) ? null : cursor.getDouble(offset + 4), // longitude
            cursor.isNull(offset + 5) ? null : cursor.getDouble(offset + 5), // latitude
            cursor.isNull(offset + 6) ? null : cursor.getDouble(offset + 6) // altitude
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Location entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setFlag_path(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setCountry(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setPlace(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setLongitude(cursor.isNull(offset + 4) ? null : cursor.getDouble(offset + 4));
        entity.setLatitude(cursor.isNull(offset + 5) ? null : cursor.getDouble(offset + 5));
        entity.setAltitude(cursor.isNull(offset + 6) ? null : cursor.getDouble(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Location entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Location entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
