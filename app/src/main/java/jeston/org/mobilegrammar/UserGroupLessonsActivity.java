package jeston.org.mobilegrammar;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static jeston.org.mobilegrammar.R.string.groups_have_been_removed;
import static jeston.org.mobilegrammar.R.string.message_about_removing;
import static jeston.org.mobilegrammar.R.string.message_cancel;
import static jeston.org.mobilegrammar.R.string.message_ok;
import static jeston.org.mobilegrammar.R.string.remove_your_groups;

public class UserGroupLessonsActivity extends AppCompatActivity {

    // instance of object with database functions
    private ArticlesDataSource mDbHelper;
    // listview instance
    private ListView userGroupLessonsListView;
    // instance of adapter with checkbox and textview
    private CursorAdapter userGroupLessonsViewAdapter;
    // constant to define what maximum groups must be in listview to turn of search field
    private static final int LIMIT_TO_SHOW_SEARCH_FIELD = 10;

    TextView searchTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbarWithBackButton();

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_user_group_lessons);
        View inflated = stub.inflate();

        userGroupLessonsListView = (ListView) findViewById(R.id.listViewUserGroups);

        com.melnykov.fab.FloatingActionButton fab = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(userGroupLessonsListView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to top of listview
                userGroupLessonsListView.setSelectionAfterHeaderView();
            }
        });
        mDbHelper = new ArticlesDataSource(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();
        // get all group's names to select their in list view
        Cursor groupsNameCursor = mDbHelper.getAllGroupsName();

        userGroupLessonsViewAdapter = new LessonsGroupCursorAdapter(this, groupsNameCursor, 0);
        userGroupLessonsListView.setAdapter(userGroupLessonsViewAdapter);

        // if listview with groups is empty ....
        if (userGroupLessonsListView.getCount() == 0) {
            // ... show textview with text about this
            TextView emptyListUserGroupTextView = (TextView) findViewById(R.id.emptyListUserGroupTextView);
            emptyListUserGroupTextView.setVisibility(View.VISIBLE);
        } else if (userGroupLessonsListView.getCount() > 0) {
            // ... otherwise hide this textview (just in case)
            TextView emptyListUserGroupTextView = (TextView) findViewById(R.id.emptyListUserGroupTextView);
            emptyListUserGroupTextView.setVisibility(View.INVISIBLE);

            // if row's count in listview greater than constant ...
            if (userGroupLessonsListView.getCount() > LIMIT_TO_SHOW_SEARCH_FIELD) {
                // ... so there is sense to show search field
                searchTextView = (TextView) findViewById(R.id.editTextSearchField);
                searchTextView.setVisibility(View.VISIBLE);
                searchTextView.addTextChangedListener(textWatcher);

                // init fab and set event to scroll to top of listview
                fab.attachToListView(userGroupLessonsListView);
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // go to top of listview
                        userGroupLessonsListView.setSelectionAfterHeaderView();
                    }
                });
            } else {
                //... if row's count is not enough, then hide search field
                TextView searchTextView = (TextView) findViewById(R.id.editTextSearchField);
                searchTextView.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.INVISIBLE);
            }
            // go to see the list of lessons of clicked listview's item
            userGroupLessonsListView.setOnItemClickListener(listViewItemListener);

            // long click to select group to remove it
            userGroupLessonsListView.setLongClickable(true);

            registerForContextMenu(userGroupLessonsListView);
        }

    }

    /**
     * Just code of toolbar init, which is replaced to function to be brief
     */
    private void initToolbarWithBackButton() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_lisview_groups_operation, menu);

    }

    /**
     * Provides two actions: edit and hide a group
     *
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // get data of row where context menu has been fired
        int groupIdToRemove = info.position;
        /**
         * When we click on listview, we get variable "position", which counts since 0, having in mind just simple order of row.
         * In order to edit group we must to define his id from database
         */
        Cursor c = (Cursor) userGroupLessonsViewAdapter.getItem(groupIdToRemove);
        Long id = c.getLong(c.getColumnIndexOrThrow("_id"));
        // We extract name of group if user would want to edit name
        String groupNameToEdit = c.getString(c.getColumnIndexOrThrow("title"));

        switch (item.getItemId()) {
            case R.id.context_menu_edit_group:
                // edit group
                Intent intent = new Intent(getApplicationContext(), FormCreateNewGroupActivity.class);
                intent.putExtra("edit_group", id);
                intent.putExtra("group_name", groupNameToEdit);
                startActivity(intent);
                return true;
            case R.id.context_menu_remove_group:
                // remove group. User must confirm the removing
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.drawer_icon)
                        .setTitle(R.string.removing_group)
                        .setMessage(R.string.this_will_remove_selected_group)
                        .setPositiveButton(message_ok, new AlertRemoveGroupById(id))
                        .setNegativeButton(message_cancel, null)
                        .show();
                return true;
            default:
                return super.onContextItemSelected(item);
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
        getMenuInflater().inflate(R.menu.add_new_group_menu, menu);
        return true;
    }

    /**
     * Provides the functions of the action bar
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
            if (id == R.id.action_add_group) {
                // create new group - just start activity to do this
                Intent intent = new Intent(getApplicationContext(), FormCreateNewGroupActivity.class);
                startActivity(intent);
            } else {
                // remove all groups. User must confirm this
                if (id == R.id.action_remove_all_groups) {
                        new AlertDialog.Builder(this)
                                .setIcon(R.drawable.drawer_icon)
                                .setTitle(remove_your_groups)
                                .setMessage(message_about_removing)
                                .setPositiveButton(message_ok, alertRemoveAllGroupsListener)
                                .setNegativeButton(message_cancel, null)
                                .show();
                }
            }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    /**
     * Click listitem handler - go to see the list of lessons of clicked listview's item
     */
    private final AdapterView.OnItemClickListener listViewItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor c = (Cursor) parent.getItemAtPosition(position);
            String groupId = c.getString(c.getColumnIndexOrThrow("_id"));
            String groupName = c.getString(c.getColumnIndexOrThrow("title"));
            Intent intent = new Intent(getApplicationContext(), AllArticlesListViewActivity.class);
            intent.putExtra("group_id", Long.parseLong(groupId));
            intent.putExtra("group_name", groupName);
            startActivity(intent);
        }
    };

    // remove all groups listener
    private final DialogInterface.OnClickListener alertRemoveAllGroupsListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            // clear groups from database
            Cursor groupsNamesToBeDeleted = mDbHelper.removeAllGroups();
            userGroupLessonsViewAdapter.changeCursor(groupsNamesToBeDeleted);
            TextView emptyListUserGroupTextView = (TextView) findViewById(R.id.emptyListUserGroupTextView);
            emptyListUserGroupTextView.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), groups_have_been_removed, Toast.LENGTH_SHORT).show();
        }
    };


    // innder class to remove group by her id. It needs new class becouse of we need to pass the variable - id
    private class AlertRemoveGroupById implements DialogInterface.OnClickListener {

        private long groupId;

        public AlertRemoveGroupById(long groupId) {
            this.groupId = groupId;
        }

        private long getGroupId() {
            return this.groupId;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            // remove group from database
            Cursor groupsAfterRemovingSelectedGroup = mDbHelper.removeGroup(this.getGroupId());
            userGroupLessonsViewAdapter.changeCursor(groupsAfterRemovingSelectedGroup);
            if (groupsAfterRemovingSelectedGroup.getCount() == 0) {
                TextView emptyListUserGroupTextView = (TextView) findViewById(R.id.emptyListUserGroupTextView);
                emptyListUserGroupTextView.setVisibility(View.VISIBLE);
            }
            Toast.makeText(getApplicationContext(), R.string.this_group_has_been_removed, Toast.LENGTH_SHORT).show();
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
            TextView emptyTextViewText = (TextView) findViewById(R.id.emptyListUserGroupTextView);
            Cursor articlesCursor = mDbHelper.findGroups(charSequence.toString());
            if (articlesCursor.getCount() == 0) {
                userGroupLessonsListView.setVisibility(View.GONE);
                emptyTextViewText.setVisibility(View.VISIBLE);
            } else {
                // update cursor with new data - this updates the lis view
                userGroupLessonsViewAdapter.changeCursor(articlesCursor);
                userGroupLessonsListView.setVisibility(View.VISIBLE);
                emptyTextViewText.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // stub
        }
    };
}
