package jeston.org.mobilegrammar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebView;

import java.util.Locale;

public class AboutProgramActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.layout_feedback_form);
        View inflated = stub.inflate();

        initToolbarWithBackButton();

        WebView webView = (WebView) findViewById(R.id.webViewAboutProgram);
        if (BuildConfig.FLAVOR == "free") {
            switch (Locale.getDefault().getLanguage()) {
                case "uk":
                    webView.loadUrl("file:///android_asset/about/about_ua_free.html");
                    break;
                case "ru":
                    webView.loadUrl("file:///android_asset/about/about_ru_free.html");
                    break;
                default:
                    webView.loadUrl("file:///android_asset/about/about_ua_free.html");
                    break;
            }
        } else if (BuildConfig.FLAVOR == "pro") {
            switch (Locale.getDefault().getLanguage()) {
                case "uk":
                    webView.loadUrl("file:///android_asset/about/about_ua.html");
                    break;
                case "ru":
                    webView.loadUrl("file:///android_asset/about/about_ru.html");
                    break;
                default:
                    webView.loadUrl("file:///android_asset/about/about_ua.html");
                    break;
            }
        }

    }

    private void initToolbarWithBackButton() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


}
