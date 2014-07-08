package nl.basvanmarwijk.mylocations.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Closeable;

import nl.basvanmarwijk.mylocations.db.dao.DaoMaster;
import nl.basvanmarwijk.mylocations.db.dao.DaoSession;
import nl.basvanmarwijk.mylocations.db.dao.Location;
import nl.basvanmarwijk.mylocations.db.dao.LocationDao;

/**
 * @author Bas van Marwijk
 * @version 1.0 - creation
 * @since 6-7-2014
 */
public class DBManager implements Closeable {
    private static final String DATABASE_NAME = "mylocations.db";
    private static volatile boolean OPEN = false;
    private static final String TAG = DBManager.class.getName();

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    /**
     * @param applicationContext please provide an ApplicationContext
     *                           instead of an Activity context to avoid memory leaks
     */
    private DBManager(final Context applicationContext) {
        db = new DaoMaster.DevOpenHelper(applicationContext, DATABASE_NAME, null)
                .getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();

        // db has opened
        OPEN = true;

        debug();
    }

    public static DBManager createDBManager(final Context applicationContext) {
        if (!OPEN) {
            return new DBManager(applicationContext);
        } else {
            throw new IllegalStateException("DBManager already created");
        }

    }

    public Cursor getAllColumnsCursor() {
        LocationDao locationDao = daoSession.getLocationDao();
        return db.query(locationDao.getTablename(), locationDao.getAllColumns(), null,
                null, null, null, null);
    }

    private LocationDao getLocationDao() {
        return daoSession.getLocationDao();
    }

    public Location getLocationById(long id) {
        LocationDao locationDao = getLocationDao();
        return locationDao.load(id);
    }

    public void deleteLocation(Location item){
        getLocationDao().delete(item);
    }

    /**
     *
     * @param item item to insert
     * @param catchConstraintException with this flag set to false the method throws SQLiteConstraintException
     * @throws SQLiteConstraintException thrown if a constraint has triggered
     */
    public void insertLocation(Location item, boolean catchConstraintException)
            throws SQLiteConstraintException {
        try {
            getLocationDao().insert(item);
        } catch (SQLiteConstraintException e) {
            if (!catchConstraintException) {
                throw e;
            }
        }
    }

    public void debug() {
        Log.w(TAG, "Don't run in production code");
        Cursor c = getAllColumnsCursor();
        while (c.moveToNext()) {
            Log.v(TAG, "id:" + c.getLong(c.getColumnIndex(LocationDao.Properties.Id.columnName)));
        }
    }

    public void updateLocation(Location item) {
        getLocationDao().update(item);
    }

    @Override
    public void close() {
        db.close();
        daoSession.clear();

        OPEN = false;
    }
}
