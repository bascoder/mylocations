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
import nl.basvanmarwijk.mylocations.db.dao.Location_pictureDao;
import nl.basvanmarwijk.mylocations.db.dao.Location_timeDao;

/**
 * @author Bas van Marwijk
 * @version 1.1 marked CRUD operations Depecrated TODO move to separate class
 * @version 1.0 - creation
 * @since 6-7-2014
 */
public class DBManager implements Closeable {
    private static final String DATABASE_NAME = "mylocations.db";
    private static final String TAG = DBManager.class.getName();
    private static volatile boolean OPEN = false;
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

    public void debug() {
        Log.w(TAG, "Don't run in production code");
        Cursor c = getAllColumnsCursor();
        while (c.moveToNext()) {
            Log.v(TAG, "id:" + c.getLong(c.getColumnIndex(LocationDao.Properties.Id.columnName)));
        }
    }

    public Cursor getAllColumnsCursor() {
        LocationDao locationDao = daoSession.getLocationDao();
        return db.query(locationDao.getTablename(), locationDao.getAllColumns(), null,
                null, null, null, null);
    }

    public static DBManager createDBManager(final Context applicationContext) {
        if (!OPEN) {
            return new DBManager(applicationContext);
        } else {
            throw new IllegalStateException("DBManager already created");
        }

    }

    public Location_pictureDao getLocationPictureDao() {
        return daoSession.getLocation_pictureDao();
    }

    public Location_timeDao getLocationTimeDao() {
        return daoSession.getLocation_timeDao();
    }

    @Deprecated
    public Location getLocationById(long id) {
        LocationDao locationDao = getLocationDao();
        return locationDao.load(id);
    }

    public LocationDao getLocationDao() {
        return daoSession.getLocationDao();
    }

    @Deprecated
    public void deleteLocation(Location item) {
        getLocationDao().delete(item);
    }

    /**
     * @param item                     item to insert
     * @param catchConstraintException with this flag set to false the method throws SQLiteConstraintException
     * @throws SQLiteConstraintException thrown if a constraint has triggered
     */
    @Deprecated
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

    @Deprecated
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
