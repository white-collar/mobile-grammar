package jeston.org.mobilegrammar;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class is form to add reminder about group and save group to database
 */
public class AddReminderToGroupActivity extends AppCompatActivity {

    // comes from intent from previous form
    private String groupName;
    // comes from intenr from previous form
    private String listOfIdsLessons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        context = getApplicationContext();
//        Button button = (Button) findViewById(R.id.test);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NotificationCompat.Builder mBuilder =
//                        new NotificationCompat.Builder(context)
//                                .setSmallIcon(R.drawable.notification_icon)
//                                .setContentTitle("My notification")
//                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
//                                .setContentText("Hello World!");
//                Intent resultIntent = new Intent(context, AllArticlesListViewActivity.class);
//
//                // The stack builder object will contain an artificial back stack for the
//                // started Activity.
//                // This ensures that navigating backward from the Activity leads out of
//                // your application to the Home screen.
//                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                // Adds the back stack for the Intent (but not the Intent itself)
//                stackBuilder.addParentStack(AllArticlesListViewActivity.class);
//                // Adds the Intent that starts the Activity to the top of the stack
//                stackBuilder.addNextIntent(resultIntent);
//                PendingIntent resultPendingIntent =
//                        stackBuilder.getPendingIntent(
//                                0,
//                                PendingIntent.FLAG_UPDATE_CURRENT
//                        );
//                mBuilder.setContentIntent(resultPendingIntent);
//                NotificationManager mNotificationManager =
//                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                // mId allows you to update the notification later on.
//                mNotificationManager.notify(1, mBuilder.build());
//            }
//        });
        //------------------------------

        initToolbarWithBackButton();

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_add_reminder_to_group);
        View inflated = stub.inflate();

        // group name from user's input
        groupName = this.getIntent().getStringExtra("created_group_name");
        // list of selected checkboxes's ids
        listOfIdsLessons = this.getIntent().getStringExtra("list_of_ids");
        //status of operation: new record or update
        StatusOfDatabaseOperation statusOperation = (StatusOfDatabaseOperation) this.
                getIntent().
                getSerializableExtra("status_operation");

        // show name of group lesson on form textview
        TextView textViewNameNewGroup = (TextView) findViewById(R.id.textViewNameNewGroup);
        textViewNameNewGroup.setText(groupName);

        // save the group lesson to database
        Button saveGroupButton = (Button) findViewById(R.id.saveButtonGroup);

        if (statusOperation == StatusOfDatabaseOperation.NEW) {
            saveGroupButton.setOnClickListener(saveGroupListener);
        } else {
            if (statusOperation == StatusOfDatabaseOperation.UPDATE) {
                long idGroupToBeUpdated = this.getIntent().getLongExtra("id_group_to_be_updated", -1);
                saveGroupButton.setOnClickListener(new UpdaterLessonGroupListener(idGroupToBeUpdated));
            }
        }
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

    // save the group lesson to database
//    private final View.OnClickListener updateGroupListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            try {
//                // save this group to database ....
//                ArticlesDataSource mDbHelper = new ArticlesDataSource(getApplicationContext());
//                mDbHelper.createDatabase();
//                mDbHelper.open();
//                long lastInsertedId = mDbHelper.saveGroup(groupName, listOfIdsLessons);
//                if (lastInsertedId != -1) {
//                    //Toast.makeText(getApplicationContext(), R.string.group_has_been_saved, Toast.LENGTH_SHORT).show();
//                    // ... and go to activity to show it
//                    Intent intent = new Intent(getApplicationContext(), AllArticlesListViewActivity.class);
//                    intent.putExtra("group_id", lastInsertedId);
//                    startActivity(intent);
//                } else {
//                    // throw exception
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    };

    // update the group lesson to database
    private final View.OnClickListener saveGroupListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                // save this group to database ....
                ArticlesDataSource mDbHelper = new ArticlesDataSource(getApplicationContext());
                mDbHelper.createDatabase();
                mDbHelper.open();
                long lastInsertedId = mDbHelper.saveGroup(groupName, listOfIdsLessons);
                if (lastInsertedId != -1) {
                    //Toast.makeText(getApplicationContext(), R.string.group_has_been_saved, Toast.LENGTH_SHORT).show();
                    // ... and go to activity to show it
                    Intent intent = new Intent(getApplicationContext(), AllArticlesListViewActivity.class);
                    intent.putExtra("group_id", lastInsertedId);
                    intent.putExtra("status_what_show", ActivityArticlesStatusToShow.SHOW_ALL_GROUPS);
                    startActivity(intent);
                } else {
                    // throw exception
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    private final class UpdaterLessonGroupListener implements View.OnClickListener {

        private long idGroupToBeUpdated;

        public UpdaterLessonGroupListener(long idGroupToBeUpdated) {
            this.idGroupToBeUpdated = idGroupToBeUpdated;
        }

        private long getIdGroupToBeUpdated() {
            return this.idGroupToBeUpdated;
        }

        @Override
        public void onClick(View view) {
            try {
                // update this group to database ....
                ArticlesDataSource mDbHelper = new ArticlesDataSource(getApplicationContext());
                mDbHelper.createDatabase();
                mDbHelper.open();
                long updatedId = mDbHelper.updateGroup(groupName, listOfIdsLessons, getIdGroupToBeUpdated());
                if (updatedId != -1) {
                    //Toast.makeText(getApplicationContext(), R.string.group_has_been_saved, Toast.LENGTH_SHORT).show();
                    // ... and go to activity to show it
                    Intent intent = new Intent(getApplicationContext(), AllArticlesListViewActivity.class);
                    intent.putExtra("group_id", getIdGroupToBeUpdated());
                    intent.putExtra("status_what_show", ActivityArticlesStatusToShow.SHOW_ALL_GROUPS);
                    startActivity(intent);
                } else {
                    // throw exception
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
