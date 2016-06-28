package jeston.org.mobilegrammar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeston on 21.06.2016.
 */
public class LessonsWithCheckboxCursorAdapter extends CursorAdapter {

    private LinearLayout topLayoutLessonGroup;
    List<Integer> selectedItemsPositions;

    public LessonsWithCheckboxCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_checkbox_listview_item, parent, false);
        topLayoutLessonGroup = (LinearLayout) parent.getRootView().findViewById(R.id.linearLayoutUserGroupName);
        selectedItemsPositions = new ArrayList<>();
        CheckBox box = (CheckBox) view.findViewById(R.id.checkboxAddLessonToGroup);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = (int) compoundButton.getTag();
                if (b) {
                    //check whether its already selected or not
                    if (!selectedItemsPositions.contains(position))
                        selectedItemsPositions.add(position);
                } else {
                    //remove position if unchecked checked item
                    selectedItemsPositions.remove((Object) position);
                }
                // manage the visibility of layout with edittext and button
                if (selectedItemsPositions.size() == 0) {
                    topLayoutLessonGroup.getLayoutParams().height = 0;
                    topLayoutLessonGroup.requestLayout();
                } else {
                    topLayoutLessonGroup.getLayoutParams().height = 90;
                    topLayoutLessonGroup.requestLayout();
                }
            }
        });
        return view;
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        CheckBox box = (CheckBox) view.findViewById(R.id.checkboxAddLessonToGroup);
        box.setTag(cursor.getPosition());

        if (selectedItemsPositions.contains(cursor.getPosition()))
            box.setChecked(true);
        else
            box.setChecked(false);

        //Find fields to populate in inflated template
        TextView lessonToSelectTitle = (TextView) view.findViewById(R.id.lessonToSelectTitle);
        // Extract properties from cursor
        String lessonToSelectTitleString = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        lessonToSelectTitle.setText(lessonToSelectTitleString);
    }

    public String getListOfIndexesSelectedCheckboxes() {
        ArrayList<Integer> correctedOnPlusOneItemsPositions = new ArrayList<>();
        Log.w("list 1", android.text.TextUtils.join(",", selectedItemsPositions));
        for (int i = 0; i < selectedItemsPositions.size(); i++) {
            correctedOnPlusOneItemsPositions.add(selectedItemsPositions.get(i).intValue() + 1);
        };
        Log.w("list 2", android.text.TextUtils.join(",", correctedOnPlusOneItemsPositions));
        return android.text.TextUtils.join(",", correctedOnPlusOneItemsPositions);
    }

}