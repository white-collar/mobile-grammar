package jeston.org.mobilegrammar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AboutProgramActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private EditText email;
    private EditText messageText;
    private Spinner  spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.layout_feedback_form);
        View inflated = stub.inflate();

        initToolbarWithBackButton();

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.theme_feedback, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        messageText = (EditText) findViewById(R.id.messageFeedback);

        email = (EditText) findViewById(R.id.emailFeedback);

        Button buttonSendFeedback = (Button) findViewById(R.id.sendFeedbackButton);
        buttonSendFeedback.setOnClickListener(sendFeedbackListener);
    }

    protected void initToolbarWithBackButton() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.feedback_string);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private final View.OnClickListener sendFeedbackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (TextUtils.isEmpty(messageText.getText().toString())){
                messageText.setError(getString(R.string.message_feedback_not_empty));
                return;
            }
            String key = mDatabase.child("messages").push().getKey();
            FeedbackDataClass message = new FeedbackDataClass(
                    key,
                    email.getText().toString(),
                    messageText.getText().toString(),
                    spinner.getSelectedItem().toString());
            Map<String, Object> messageValues = message.toMap();
            Map<String, Object> messageUpdates = new HashMap<>();
            messageUpdates.put("/messages/" + key, messageValues);
            mDatabase.updateChildren(messageUpdates);
            Toast.makeText(getApplicationContext(), R.string.thanks_for_feedback, Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.privacy_statement, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.context_menu_privacy_statement) {
            String path = "file:///android_asset/privacy_statement.html";
            Uri uri = Uri.fromFile(new File(path));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
            browserIntent.setData(uri);
            startActivity(browserIntent);
        }

        return super.onOptionsItemSelected(item);
    }


}
