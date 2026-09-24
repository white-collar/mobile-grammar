package jeston.org.mobilegrammar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Installed apps keep the database copied on first launch, so wrong titles are corrected when it is opened.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = 36)
public class LessonTitlesFixTest {

    @Test
    public void oldTitlesAreCorrectedAndUserGroupsKept() {
        Context context = ApplicationProvider.getApplicationContext();
        ArticlesDataSource dataSource = new ArticlesDataSource(context).createDatabase().open();
        dataSource.close();

        // database of an installed app: titles of the first versions and a group created by user
        SQLiteDatabase db = SQLiteDatabase.openDatabase(
                context.getDatabasePath("db11.db").getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        db.execSQL("update articles set unit_number = 'Unit 52 - Question tags (are you? doesn&apos;t he? etc.)' where _id = 52");
        db.execSQL("update articles set unit_number = 'Unit 106 - Word order (1) - verb + object; place and time' where _id = 106");
        db.execSQL("insert into groups_lesson (name, ids) values ('My group', '52,106')");
        db.close();

        dataSource = new ArticlesDataSource(context).createDatabase().open();
        Map<Long, String> titles = new HashMap<>();
        Cursor cursor = dataSource.getAllArticles();
        do {
            titles.put(cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")));
        } while (cursor.moveToNext());
        cursor.close();

        assertEquals(130, titles.size());
        assertEquals("Unit 52 - Question tags (are you? doesn't he? etc.)", titles.get(52L));
        assertEquals("Unit 106 - Word order (2) - adverbs with the verb", titles.get(106L));
        assertEquals("Unit 105 - Word order (1) - verb + object; place and time", titles.get(105L));
        for (String title : titles.values()) {
            assertFalse(title, title.contains("&apos;"));
        }

        Cursor groups = dataSource.getAllGroupsName();
        assertEquals(1, groups.getCount());
        assertEquals("My group", groups.getString(groups.getColumnIndexOrThrow("title")));
        assertEquals("52,106", groups.getString(groups.getColumnIndexOrThrow("ids")));
        groups.close();
        dataSource.close();
    }
}
