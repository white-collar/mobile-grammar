package jeston.org.mobilegrammar;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

public class AllArticlesListViewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArticlesDataSource mDbHelper;
    private ListView articlesListViewItems;
    private ArticleCursorAdapter articlesViewAdapter;
    private Cursor articlesCursor;

    private ActivityArticlesStatusToShow statusToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.layout_all_articles_list_view);
        View inflated = stub.inflate();

        articlesListViewItems = (ListView) findViewById(R.id.listViewArticles);

        mDbHelper = new ArticlesDataSource(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // get data from intent to define what's group to show
        long groupId = this.getIntent().getLongExtra("group_id", -1);
        statusToShow = (ActivityArticlesStatusToShow) this.getIntent().getSerializableExtra("status_what_show");

        if (groupId == 0 || groupId == -1) {
            initDrawer();
            articlesCursor = mDbHelper.getAllArticles();
        } else {
            initToolbarWithBackButton();
            articlesCursor = mDbHelper.getArticlesByGroup(groupId);
        }

        articlesListViewItems.setOnItemClickListener(listViewClickListener);
        if (articlesViewAdapter == null) {
            articlesViewAdapter = new ArticleCursorAdapter(this, articlesCursor, 0);
        } else
            articlesViewAdapter.changeCursor(articlesCursor);

        // Attach cursor adapter to the ListView
        articlesListViewItems.setAdapter(articlesViewAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (articlesListViewItems.getCount() > 10) {
            TextView searchTextView = (TextView) findViewById(R.id.editTextSearchField);
            searchTextView.setVisibility(View.VISIBLE);
            searchTextView.addTextChangedListener(textWatcher);

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
            TextView searchTextView = (TextView) findViewById(R.id.editTextSearchField);
            searchTextView.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
        }
    }

    protected void initDrawer() {
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

    protected void initToolbarWithBackButton() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
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
            super.onBackPressed();
//            if (statusToShow != null) {
//                articlesCursor = mDbHelper.getAllGroupsName();
//                articlesViewAdapter.changeCursor(articlesCursor);
//            } else {
//                super.onBackPressed();
//            }
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
            Intent intent = new Intent(getApplicationContext(), FeedbackActivity.class);
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
        } else if (id == R.id.menu_user_groups) {
            Intent intent = new Intent(this, UserGroupLessonsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            TextView emptyTextViewText = (TextView) findViewById(R.id.emptyListTextView);
            Cursor articlesCursor = mDbHelper.findArticles(charSequence.toString());
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

        }
    };

    AdapterView.OnItemClickListener listViewClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor c = (Cursor) parent.getItemAtPosition(position);
            String html = c.getString(c.getColumnIndexOrThrow("html"));
            Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
            intent.putExtra("html", html);
            startActivity(intent);
        }
    };
}
