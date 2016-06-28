package jeston.org.mobilegrammar;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import static jeston.org.mobilegrammar.R.string.groups_have_been_removed;
import static jeston.org.mobilegrammar.R.string.message_about_removing;
import static jeston.org.mobilegrammar.R.string.message_cancel;
import static jeston.org.mobilegrammar.R.string.message_ok;
import static jeston.org.mobilegrammar.R.string.navigation_drawer_close;
import static jeston.org.mobilegrammar.R.string.navigation_drawer_open;
import static jeston.org.mobilegrammar.R.string.remove_your_groups;

public class UserGroupLessonsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArticlesDataSource mDbHelper;
    private ListView userGroupLessonsListView;
    private CursorAdapter userGroupLessonsViewAdapter;
    /*
     this defines max id of system group. It used in OnCreateContextMenu to show it only on user
     custom groups
      */
    private static final int ID_OF_BORDER_OF_SYSTEM_GROUP = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, navigation_drawer_open, navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        // hide block with title about empty list view
        // TODO: 27.06.2016 may be it's redudant becouse of user can't remove system groups - so listview can't be empty
        LinearLayout layout = (LinearLayout) findViewById(R.id.lin_all);
        layout.setVisibility(View.INVISIBLE);

        userGroupLessonsViewAdapter = new LessonsGroupCursorAdapter(this, groupsNameCursor, 0);
        userGroupLessonsListView.setAdapter(userGroupLessonsViewAdapter);

        // go to see the list of lessons of clicked listview's item
        userGroupLessonsListView.setOnItemClickListener(listViewItemListener);

        // long click to select group to remove it
        userGroupLessonsListView.setLongClickable(true);

        registerForContextMenu(userGroupLessonsListView);

    }

    /**
     * We show context menu only on user's custon group. Removing the system groups is not allowed
     *
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listViewUserGroups) {
            // Get the info on which item was selected
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            if (info.position >= ID_OF_BORDER_OF_SYSTEM_GROUP) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.context_menu_lisview_groups_operation, menu);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // get data of row where context menu has been fired
        int groupIdToRemove = info.position;
        Cursor c = (Cursor) userGroupLessonsViewAdapter.getItem(groupIdToRemove);
        Long id = c.getLong(c.getColumnIndexOrThrow("_id"));
        String groupNameToEdit = c.getString(c.getColumnIndexOrThrow("title"));
        switch (item.getItemId()) {
            case R.id.context_menu_edit_group:
                Intent intent = new Intent(getApplicationContext(), FormCreateNewGroupActivity.class);
                intent.putExtra("edit_group", id);
                intent.putExtra("group_name", groupNameToEdit);
                startActivity(intent);
                return true;
            case R.id.context_menu_remove_group:
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else {
            if (id == R.id.action_add_group) {
                Intent intent = new Intent(getApplicationContext(), FormCreateNewGroupActivity.class);
                startActivity(intent);
            } else {
                if (id == R.id.action_remove_all_groups) {
                    if (userGroupLessonsViewAdapter.getCount() > 4) {
                        new AlertDialog.Builder(this)
                                .setIcon(R.drawable.drawer_icon)
                                .setTitle(remove_your_groups)
                                .setMessage(message_about_removing)
                                .setPositiveButton(message_ok, alertRemoveAllGroupsListener)
                                .setNegativeButton(message_cancel, null)
                                .show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.removing_system_group_is_not_allowed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
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
     * Click listitem handler - go to see the list of lessons of clicked listview's item
     */
    private final AdapterView.OnItemClickListener listViewItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor c = (Cursor) parent.getItemAtPosition(position);
            String groupId = c.getString(c.getColumnIndexOrThrow("_id"));
            Intent intent = new Intent(getApplicationContext(), AllArticlesListViewActivity.class);
            intent.putExtra("group_id", Long.parseLong(groupId));
            startActivity(intent);
        }
    };

    private final DialogInterface.OnClickListener alertRemoveAllGroupsListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            // clear groups from database
            Cursor groupsNamesToBeDeleted = mDbHelper.removeAllGroups();
            userGroupLessonsViewAdapter.changeCursor(groupsNamesToBeDeleted);
            Toast.makeText(getApplicationContext(), groups_have_been_removed, Toast.LENGTH_SHORT).show();
        }
    };


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
            Toast.makeText(getApplicationContext(), R.string.this_group_has_been_removed, Toast.LENGTH_SHORT).show();
        }
    }
}
