package jeston.org.mobilegrammar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebView;

import java.util.Date;

public class WebViewActivity extends AppCompatActivity {

    String lessonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String html = this.getIntent().getStringExtra("html");
        lessonName = this.getIntent().getStringExtra("lesson_name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(lessonName);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_web_view);
        View inflated = stub.inflate();

        String mime = "text/html";
        String encoding = "utf-8";

        WebView myWebView = (WebView) findViewById(R.id.webViewArcticle);


        myWebView.getSettings().setJavaScriptEnabled(false);
        myWebView.loadDataWithBaseURL(null, html, mime, encoding, null);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.create_reminder_menu_item) {
            Intent intent = new Intent();

            // mimeType will popup the chooser any  for any implementing application (e.g. the built in calendar or applications such as "Business calendar"
            intent.setType("vnd.android.cursor.item/event");

            // the time the event should start in millis. This example uses now as the start time and ends in 1 hour
            intent.putExtra("beginTime", new Date().getTime());
            intent.putExtra("endTime", new Date().getTime() + DateUtils.HOUR_IN_MILLIS);

            intent.putExtra("title", getString(R.string.text_reminder_about_lesson));
            intent.putExtra("description", getString(R.string.text_reminder_about_lesson) + ":" + lessonName);

            // the action
            intent.setAction(Intent.ACTION_EDIT);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


}
