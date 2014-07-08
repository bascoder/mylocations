package nl.basvanmarwijk.mylocations;

import android.app.Application;
import android.content.Context;

import nl.basvanmarwijk.mylocations.db.DBManager;

/**
 * Wrapper class for {@link android.app.Application}
 *
 * @author Bas van Marwijk
 * @version 1.0 - creation
 * @since 3-7-2014
 */
public class App extends Application {

    private static Context appContext;
    private static DBManager dbManager;
    private static boolean DB_OPEN = false;

    private final static String E_MESSAGE = App.class.getCanonicalName()
            + " has not been initialized yet";

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();
        dbManager = DBManager.createDBManager(appContext);
        if(dbManager != null){
            DB_OPEN = true;
        }
    }

    /**
     * Get application wide context, this context lasts as long as the app
     * lifetime. Memory leak safe
     *
     * @return application context
     * @throws IllegalStateException when App has not been init
     */
    public static Context getAppContext() throws IllegalStateException {
        if (appContext == null) {
            throw new IllegalStateException(E_MESSAGE);
        }
        return appContext;
    }

    public static DBManager getDbManager() throws IllegalStateException {
        if (dbManager == null) {
            throw new IllegalStateException(E_MESSAGE);
        }
        return dbManager;
    }
}
