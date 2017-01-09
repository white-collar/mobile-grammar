package jeston.org.mobilegrammar;

import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Database tester class.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    // database name from asset folder
    private static final String DATABASE_NAME = "db10.db";
    // database version
    private static final int DATABASE_VERSION = 11;
    // database path (will be assigned in runtime)
    private static String DATABASE_PATH = "";

    @Test
    public void assignPathesToDatabase() {
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DATABASE_PATH = getTargetContext().getApplicationInfo().dataDir + "/databases/";
        } else {
            DATABASE_PATH = "/data/data/" + getTargetContext().getPackageName() + "/databases/";
        }
        assertThat(DATABASE_PATH, containsString("org.mobile.grammar"));
    }

    @Test
    public void openDatabase() {
        ArticlesDataSource mDbHelper = new ArticlesDataSource(getTargetContext());
        mDbHelper.createDatabase();
        mDbHelper.open();
        mDbHelper.close();
    }

    @Test
    public void howManyRecordsInDatabase() {
        ArticlesDataSource mDbHelper = new ArticlesDataSource(getTargetContext());
        mDbHelper.createDatabase();
        mDbHelper.open();
        Cursor cursor = mDbHelper.getAllArticles();

        assertTrue(cursor.getCount() == 130);
    }

    @Test
    public void isGroupsNameTableOK() {
        ArticlesDataSource mDbHelper = new ArticlesDataSource(getTargetContext());
        mDbHelper.createDatabase();
        mDbHelper.open();
        Cursor cursor = mDbHelper.getAllGroupsName();
        Log.w("w", String.valueOf(cursor.getCount()));
        assertTrue(cursor.getCount() > 0);
    }


}
