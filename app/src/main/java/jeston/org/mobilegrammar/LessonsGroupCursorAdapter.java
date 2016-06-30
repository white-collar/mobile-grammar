package jeston.org.mobilegrammar;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * Created by Jeston on 21.06.2016.
 */
public class LessonsGroupCursorAdapter extends CursorAdapter {

    public LessonsGroupCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.layout_simple_user_group_listview_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView textViewArticleTitle = (TextView) view.findViewById(R.id.itemLessonGroupName);
        // Extract properties from cursor
        String articleTitle = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        textViewArticleTitle.setText(articleTitle);
    }
}