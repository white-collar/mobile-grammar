package jeston.org.mobilegrammar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FormCreateNewGroupActivity extends AppCompatActivity {

    private ArticlesDataSource mDbHelper;
    private ListView lessonsListViewItems;
    private LessonsWithCheckboxCursorAdapter lessonsViewAdapter;
    private Cursor lessonsCursor;

    private ArrayList<Integer> idLessonsToGroup;

    // setting's file to store that snackbar has been shown
    private static final String APP_PREFERENCES = "settings";
    private static final String PREFERENCES_SETTING_NAME = "snackbar_was_shown";

    private static final byte MAX_LENGTH_GROUP_NAME = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_form_create_new_group);
        View inflated = stub.inflate();

        lessonsListViewItems = (ListView) findViewById(R.id.listViewLessonsToSelect);

        com.melnykov.fab.FloatingActionButton fab = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(lessonsListViewItems);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to top of listview
                lessonsListViewItems.setSelectionAfterHeaderView();
            }
        });

        // get name of group and go to saving in database
        ImageButton goToCreateReminderButton = (ImageButton) findViewById(R.id.goToCreateReminder);

        mDbHelper = new ArticlesDataSource(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // define if we need to edit or create group
        if (this.getIntent().getLongExtra("edit_group", -1) != -1) {
            // editing group
            long id = this.getIntent().getLongExtra("edit_group", -1);
            String groupName = this.getIntent().getStringExtra("group_name");
            lessonsCursor = mDbHelper.getArticlesByGroup(id);
            lessonsViewAdapter = new LessonsWithCheckboxCursorAdapter(this, lessonsCursor, 0);
            lessonsListViewItems.setAdapter(lessonsViewAdapter);
            // set name of textview
            TextView editTextLessonName = (TextView) findViewById(R.id.editTextLessonName);
            editTextLessonName.setText(groupName);

            initToolbarWithBackButton(getString(R.string.text_about_updating));
            goToCreateReminderButton.setOnClickListener(new SaverGroupToDatabase(StatusOfDatabaseOperation.UPDATE, id));
        } else {
            // just create group - show all lessons
            lessonsCursor = mDbHelper.getAllArticles();
            lessonsViewAdapter = new LessonsWithCheckboxCursorAdapter(this, lessonsCursor, 0);
            lessonsListViewItems.setAdapter(lessonsViewAdapter);

            initToolbarWithBackButton(null);
            goToCreateReminderButton.setOnClickListener(new SaverGroupToDatabase(StatusOfDatabaseOperation.NEW));
        }

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(PREFERENCES_SETTING_NAME)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREFERENCES_SETTING_NAME, true);
            editor.apply();
            Snackbar.make(lessonsListViewItems, R.string.propose_user_to_select_lessons, Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    private void initToolbarWithBackButton(String textAboutUpdating) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (textAboutUpdating != null) {
            getSupportActionBar().setTitle(textAboutUpdating);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    // it uses to listen the user's input and make SQL with LIKE to database
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // stub
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            TextView emptyTextViewText = (TextView) findViewById(R.id.emptyListTextViewGroups);
            Cursor articlesCursor = mDbHelper.findArticles(charSequence.toString(), null);
            if (articlesCursor.getCount() == 0) {
                lessonsListViewItems.setVisibility(View.GONE);
                emptyTextViewText.setVisibility(View.VISIBLE);
            } else {
                // update cursor with new data - this updates the lis view
                lessonsViewAdapter.changeCursor(articlesCursor);
                lessonsListViewItems.setVisibility(View.VISIBLE);
                emptyTextViewText.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // stub
        }
    };



    /**
     * Add ids of selected checkboxes to array list to save their into database
     */
    private class SaverGroupToDatabase implements View.OnClickListener {
        private StatusOfDatabaseOperation statusOperation;
        private long idGroupToBeUpdated;

        public SaverGroupToDatabase(StatusOfDatabaseOperation operation) {
            this.statusOperation = operation;
        }

        public SaverGroupToDatabase(StatusOfDatabaseOperation operation, long idGroupToBeUpdated) {
            this.statusOperation = operation;
            this.idGroupToBeUpdated = idGroupToBeUpdated;
        }

        private StatusOfDatabaseOperation getOperation() {
            return this.statusOperation;
        }

        private long getIdGroupToBeUpdated() {
            return this.idGroupToBeUpdated;
        }
        @Override
        public void onClick(View view) {
            String listOfIds = lessonsViewAdapter.getListOfIndexesSelectedCheckboxes();
            EditText editTextLessonName = (EditText)findViewById(R.id.editTextLessonName);
            String groupName = editTextLessonName.getText().toString();
            if (groupName.length() == 0) {
                // if no one checkbox is selected - show toast about this
                Toast.makeText(getApplicationContext(), R.string.no_group_name_entered_by_user, Toast.LENGTH_SHORT).show();
            } else if (groupName.length() > MAX_LENGTH_GROUP_NAME) {
                editTextLessonName.setText("");
                Toast.makeText(getApplicationContext(), R.string.too_long_group_name, Toast.LENGTH_SHORT).show();
            } else {
                {
                    Intent intent = new Intent(getApplicationContext(), AddReminderToGroupActivity.class);
                    intent.putExtra("created_group_name", groupName);
                    intent.putExtra("list_of_ids", listOfIds);
                    intent.putExtra("status_operation", getOperation());
                    intent.putExtra("id_group_to_be_updated", getIdGroupToBeUpdated());
                    startActivity(intent);
                }
            }
        }
    }
}
