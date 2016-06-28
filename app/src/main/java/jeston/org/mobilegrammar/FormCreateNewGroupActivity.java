package jeston.org.mobilegrammar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FormCreateNewGroupActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArticlesDataSource mDbHelper;
    private ListView lessonsListViewItems;
    private LessonsWithCheckboxCursorAdapter lessonsViewAdapter;
    private Cursor lessonsCursor;

    private ArrayList<Integer> idLessonsToGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

            goToCreateReminderButton.setOnClickListener(new SaverGroupToDatabase(StatusOfDatabaseOperation.UPDATE, id));
        } else {
            // just create group - show all lessons
            lessonsCursor = mDbHelper.getAllArticles();
            lessonsViewAdapter = new LessonsWithCheckboxCursorAdapter(this, lessonsCursor, 0);
            lessonsListViewItems.setAdapter(lessonsViewAdapter);

            goToCreateReminderButton.setOnClickListener(new SaverGroupToDatabase(StatusOfDatabaseOperation.NEW));
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.menu_all_articles) {
            Intent intent = new Intent(this, AllArticlesListViewActivity.class);
            intent.putExtra("group_id", GroupId.mainGroup.getValue());
            startActivity(intent);
        } else if (id == R.id.menu_group1) {
            Intent intent = new Intent(this, AllArticlesListViewActivity.class);
            intent.putExtra("group_id", GroupId.group1.getValue());
            startActivity(intent);
        } else if (id == R.id.menu_group2) {
            Intent intent = new Intent(this, AllArticlesListViewActivity.class);
            intent.putExtra("group_id", GroupId.group2.getValue());
            startActivity(intent);
        } else if (id == R.id.menu_group3) {
            Intent intent = new Intent(this, AllArticlesListViewActivity.class);
            intent.putExtra("group_id", GroupId.group3.getValue());
            startActivity(intent);
        } else if (id == R.id.menu_group4) {
            Intent intent = new Intent(this, AllArticlesListViewActivity.class);
            intent.putExtra("group_id", GroupId.group4.getValue());
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
            } else {
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
