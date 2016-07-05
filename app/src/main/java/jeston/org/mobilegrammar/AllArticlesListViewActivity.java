package jeston.org.mobilegrammar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

public class AllArticlesListViewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArticlesDataSource mDbHelper;
    private ListView articlesListViewItems;
    private ArticleCursorAdapter articlesViewAdapter;
    private Cursor articlesCursor;
    // must be initialized by null
    private String sequenceIds = null;

    private ActivityArticlesStatusToShow statusToShow;

    // constant to define what maximum groups must be in listview to turn of search field
    private static final int LIMIT_TO_SHOW_SEARCH_FIELD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // load layout with activity
        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.layout_all_articles_list_view);
        View inflated = stub.inflate();
        // listview with articles
        articlesListViewItems = (ListView) findViewById(R.id.listViewArticles);
        // database init
        mDbHelper = new ArticlesDataSource(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // get data from intent to define what's group to show
        long groupId = this.getIntent().getLongExtra("group_id", -1);
        statusToShow = (ActivityArticlesStatusToShow) this.getIntent().getSerializableExtra("status_what_show");
        String groupName = this.getIntent().getStringExtra("group_name");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.all_articles_menu_title));

        // if there is start activity, so show drawer
        if (groupId == 0 || groupId == -1) {
            initDrawer();
            articlesCursor = mDbHelper.getAllArticles();
        } else {
            // ... but if this activity is used to show short listview with articles, just show toolbar with back button
            initToolbarWithBackButton(groupName);
            articlesCursor = mDbHelper.getArticlesByGroup(groupId);
            sequenceIds = mDbHelper.getIdsOfGroup(groupId);
        }
        // listener of row click
        articlesListViewItems.setOnItemClickListener(listViewClickListener);

        // if we just open application - adapter has never been used, so create this
        if (articlesViewAdapter == null) {
            articlesViewAdapter = new ArticleCursorAdapter(this, articlesCursor, 0);
        } else
        /**
         * otherwise it means that this activity is used inside app - to show short list of articles
         * just change cursor
         */
            articlesViewAdapter.changeCursor(articlesCursor);

        // Attach cursor adapter to the ListView
        articlesListViewItems.setAdapter(articlesViewAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // if row's count greater that limit ,,,
        if (articlesListViewItems.getCount() > LIMIT_TO_SHOW_SEARCH_FIELD) {
            // fab link to listview
            fab.attachToListView(articlesListViewItems);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // go to top of listview
                    articlesListViewItems.setSelectionAfterHeaderView();
                }
            });
        }
        else {
            // otherwise - hide fab
            fab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    /**
     * Just code of drawer init, which is cutted to function to be brief
     */
    private void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Just code of toolbar init, which is cutted to function to be brief
     */
    private void initToolbarWithBackButton(String groupName) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (groupName != null) {
            getSupportActionBar().setTitle(groupName);
        }
        else {
            getSupportActionBar().setTitle(getString(R.string.all_articles_menu_title));
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
            // back to all groups
            // super.onBackPressed();
            if (statusToShow != null) {
                Log.w("true", statusToShow.toString());
                Intent intent = new Intent(this, UserGroupLessonsActivity.class);
                startActivity(intent);
            } else {
                Log.w("false", "false");
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (sequenceIds == null) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_show_search_field) {
            // change layout params to show search text
            EditText searchTextView = (EditText) findViewById(R.id.editTextSearchField);
            // text listener to use entered characters in search
            searchTextView.addTextChangedListener(textWatcher);
            // input manager to start or hide keayboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (searchTextView.getLayout().getWidth() == 0) {
                // show text for entering search field
                searchTextView.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                // ... and set focus
                searchTextView.requestFocus();
                // show keyboard
                imm.showSoftInput(searchTextView, InputMethodManager.SHOW_IMPLICIT);
            } else {
                if (searchTextView.getText().toString().length() > 0) return true;
                // hide search field
                searchTextView.setLayoutParams(new Toolbar.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
                // hide keyboard
                imm.hideSoftInputFromWindow(searchTextView.getWindowToken(), 0);
            }
            return true;
        } else if (id == R.id.about_program_menu_item) {
            Intent intent = new Intent(getApplicationContext(), AboutProgramActivity.class);
            startActivity(intent);
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
            intent.putExtra("group_name", item.getTitle());
            startActivity(intent);
        } else if (id == R.id.menu_group2) {
            Intent intent = new Intent(this, AllArticlesListViewActivity.class);
            intent.putExtra("group_id", GroupId.group2.getValue());
            intent.putExtra("group_name", item.getTitle());
            startActivity(intent);
        } else if (id == R.id.menu_group3) {
            Intent intent = new Intent(this, AllArticlesListViewActivity.class);
            intent.putExtra("group_id", GroupId.group3.getValue());
            intent.putExtra("group_name", item.getTitle());
            startActivity(intent);
        } else if (id == R.id.menu_group4) {
            Intent intent = new Intent(this, AllArticlesListViewActivity.class);
            intent.putExtra("group_id", GroupId.group4.getValue());
            intent.putExtra("group_name", item.getTitle());
            startActivity(intent);
        } else if (id == R.id.menu_user_groups) {
            Intent intent = new Intent(this, UserGroupLessonsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // it uses to listen the user's input and make SQL with LIKE to database
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // stub
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            TextView emptyTextViewText = (TextView) findViewById(R.id.emptyListTextView);
            Cursor articlesCursor = mDbHelper.findArticles(charSequence.toString(), sequenceIds);
            if (articlesCursor.getCount() == 0) {
                articlesListViewItems.setVisibility(View.GONE);
                emptyTextViewText.setVisibility(View.VISIBLE);
            } else {
                // update cursor with new data - this updates the lis view
                articlesViewAdapter.changeCursor(articlesCursor);
                articlesListViewItems.setVisibility(View.VISIBLE);
                emptyTextViewText.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // stub
        }
    };

    // just show html of selected article in webview
    private final AdapterView.OnItemClickListener listViewClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor c = (Cursor) parent.getItemAtPosition(position);
            String html = c.getString(c.getColumnIndexOrThrow("html"));
            String lessonName = c.getString(c.getColumnIndexOrThrow("title"));
            Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
            intent.putExtra("html", html);
            intent.putExtra("lesson_name", lessonName);
            startActivity(intent);
        }
    };
}
