package jeston.org.mobilegrammar;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class to manage articles from db
 */

public class ArticlesDBHelper extends SQLiteOpenHelper {

    // database name from asset folder
    private static final String DATABASE_NAME = "db11.db";
    // database version
    private static final int DATABASE_VERSION = 12;
    // database path (will be assigned in runtime)
    private static String DATABASE_PATH = "";
    // context of app (will be assigned in constructor)
    private final Context mContext;
    // database
    private SQLiteDatabase mDataBase;

    /**
     * Constructor to manipulate with ready database
     *
     * @param context Context of application.
     */
    public ArticlesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    /**
     * Creates database in app from file in asset folder (if not exists)
     *
     * @throws IOException
     */
    public void createDataBase() throws IOException {
        // check if database exists
        boolean mDataBaseExist = checkDataBase();
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                //Copy the database from assets
                copyDataBase();
                Log.e("DataBaseHelper", "createDatabase database created");
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    /**
     * Checks if file exists in app
     *
     * @return File file
     */
    private boolean checkDataBase() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        return dbFile.exists();
    }

    /**
     * Copies database file from assets (if need) to app
     *
     * @throws IOException
     */
    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    /**
     * Opens database to make query to it
     *
     * @return database
     * @throws SQLException
     */
    public boolean openDataBase() throws SQLException {
        String mPath = DATABASE_PATH + DATABASE_NAME;
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    /**
     * just override method from abstract class
     * @param database Abstract database.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
    }

    /**
     * just override method from abstract class
     *
     * @param db Abstract database.
     * @param oldVersion Id of old database.
     * @param newVersion Id of new database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Correctly close database
     */
    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }
}
