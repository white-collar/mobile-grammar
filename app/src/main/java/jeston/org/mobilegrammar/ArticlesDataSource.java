package jeston.org.mobilegrammar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;

/**
 * Custom class to incapsulate data handling
 */
public class ArticlesDataSource {

    // context of application (will be assigned later)
    private final Context mContext;
    // database
    private SQLiteDatabase mDb;
    // instance of database helper
    private ArticlesDBHelper mDbHelper;


    public ArticlesDataSource(Context context) {
        this.mContext = context;
        mDbHelper = new ArticlesDBHelper(mContext);
    }

    /**
     * Wrapper for createDatabase in helper. Will be called in listview activity
     *
     * @return
     * @throws SQLException
     */
    public ArticlesDataSource createDatabase() throws SQLException {
        try {
            mDbHelper.createDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    /**
     * Wrapper for openDatabase in helper. Will be called in listview activity
     *
     * @return
     * @throws SQLException
     */
    public ArticlesDataSource open() throws SQLException {
        try {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
        return this;
    }

    /**
     * Wrapper for closeDatabase in helper. Will be called in listview activity.
     */
    public void close() {
        mDbHelper.close();
    }

    /**
     * Returns all artilces to show in listview
     *
     * @return
     */
    public Cursor getAllArticles() {
        try {
            String sql = "select  _id as _id, unit_number as title, html as html from articles order by _id";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur != null) {
                mCur.moveToNext();
            }
            return mCur;
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
    }

    public Cursor findArticles(String articleName) {
        try {
            String sql = "select  _id as _id, unit_number as title, html as html from articles \n" +
                    "where unit_number like '%" + articleName + "%' order by _id ";

            Cursor mCur = mDb.rawQuery(sql, null);

            if (mCur != null) {
                mCur.moveToNext();
            }
            return mCur;
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
    }

    /**
     * Get all articles which are in created group
     *
     * @param groupId group identifier
     * @return
     */
    public Cursor getArticlesByGroup(long groupId) {
        try {

            // get all ids of lessons of selected group
            String sql = "select ids as _id from groups_lesson where _id = " + String.valueOf(groupId);

            Cursor mCur = mDb.rawQuery(sql, null);

            if (mCur != null) {
                mCur.moveToNext();
            }
            // ids of lessons to select
            String listForSQlExpression = mCur.getString(mCur.getColumnIndexOrThrow("_id"));

            String sql1 = "select  _id as _id, unit_number as title, html as html from articles \n" +
                    "where _id in (" + listForSQlExpression + ") order by _id";

            mCur = mDb.rawQuery(sql1, null);

            if (mCur != null) {
                mCur.moveToNext();
            }

            return mCur;
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
    }


    /**
     * Saves ids of selected lessons and name to database.
     *
     * @param name Name of group
     * @param ids  ids of lessons - list of number separated by comma
     */
    public long saveGroup(String name, String ids) {
        try {

            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("ids", ids);
            long rowInserted = mDb.insert("groups_lesson", null, values);

            return rowInserted;
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
    }


    /**
     * Get all groups name to show their in listview.
     *
     * @return Cursor
     */
    public Cursor getAllGroupsName() {
        String sql = "select  _id as _id, name as title, ids as ids from groups_lesson";
        Cursor mCur = mDb.rawQuery(sql, null);
        try {
            if (mCur != null) {
                mCur.moveToNext();
            }
            return mCur;
        } catch (SQLException mSQLException) {
            throw mSQLException;
        } finally {

        }
    }

    /**
     * Clear all groups (excluding 4 system group) and return updated cursor to update listview.
     *
     * @return Cursor
     */
    public Cursor removeAllGroups() {
        // remove all groups without system groups
        final int SYSTEM_ID_GROUP = 4;
        //  TODO: 27.06.2016 use delCount
        int delCount = mDb.delete("groups_lesson", "_id > " + String.valueOf(SYSTEM_ID_GROUP), null);
        return this.getAllGroupsName();
    }

    /**
     * Removes group by id (used in contextMenu in UserGroupLessonActivity)
     * @param groupId
     * @return
     */
    public Cursor removeGroup(long groupId) {
        int delCount = mDb.delete("groups_lesson", "_id = " + String.valueOf(groupId), null);
        return this.getAllGroupsName();
    }

    public long updateGroup(String groupName, String listOfIdsLessons, long idGroupToBeUpdated) {
            try {
                ContentValues values = new ContentValues();
                values.put("name", groupName);
                values.put("ids", listOfIdsLessons);
                long rowUpdated = mDb.update("groups_lesson", values, "_id = " + String.valueOf(idGroupToBeUpdated), null);
                return rowUpdated;
            } catch (SQLException mSQLException) {
                throw mSQLException;
            }
    }
}
