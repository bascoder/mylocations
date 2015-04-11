package pl.surecase.eu;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * Script to generate dao classes for green dao library
 */
public class MyDaoGenerator {

    static final String PACKAGE_NAME = "nl.basvanmarwijk.mylocations.db.dao";

    public static void main(String[] args) {
        String javaFolder = null;
        try {
            javaFolder = args[0];
        } catch (Exception e) {
            System.err.println("No valid arguments passed");

            System.exit(-1);
        }

        Schema schema = new Schema(4, PACKAGE_NAME);

        schema.setDefaultJavaPackageDao(PACKAGE_NAME);

        // Add 3 tables
        Entity location = schema.addEntity("Location");
        Entity location_time = schema.addEntity("Location_time");
        Entity location_picture = schema.addEntity("Location_picture");

        // init location table
        location.addIdProperty()
                .autoincrement();
        location.addStringProperty("flag_path");
        location.addStringProperty("country")
                .notNull();
        location.addStringProperty("place")
                .notNull()
                .unique();
        location.addDoubleProperty("longitude");
        location.addDoubleProperty("latitude");
        location.addDoubleProperty("altitude");

        // init location_time table
        location_time.addLongProperty("datetimestamp")
                .primaryKey()
                .notNull();
        Property fk = location_time
                .addLongProperty("location_id").notNull().getProperty();
        location.addToMany(location_time, fk).setName("fk_location_time");

        // init location picture table
        location_picture.addIdProperty()
                .autoincrement();
        location_picture
                .addStringProperty("picture_path")
                .notNull()
                .unique();
        Property fkp = location_picture
                .addLongProperty("location_id")
                .notNull()
                .getProperty();
        location.addToMany(location_picture, fkp)
                .setName("fk_location_picture");

        try {
            new DaoGenerator().generateAll(schema, javaFolder);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
