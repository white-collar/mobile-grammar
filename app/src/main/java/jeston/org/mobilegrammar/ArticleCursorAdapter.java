package jeston.org.mobilegrammar;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Cursor adapter for listview with articles
 */

public class ArticleCursorAdapter extends CursorAdapter {

    public ArticleCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.layout_simple_article_listview_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView textViewArticleTitle = (TextView) view.findViewById(R.id.itemListView);
//        ImageView imageListViewIcon = (ImageView) view.findViewById(R.id.iconListView);
//        int rowId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        // Extract properties from cursor
        String articleTitle = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        textViewArticleTitle.setText(articleTitle);
    }
}
