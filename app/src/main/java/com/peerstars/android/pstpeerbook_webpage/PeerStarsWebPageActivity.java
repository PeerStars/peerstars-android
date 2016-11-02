package com.peerstars.android.pstpeerbook_webpage;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.peerstars.android.R;

public class PeerStarsWebPageActivity extends AppCompatActivity {

    // build a new WebView
    WebView webView = null;

    // create a settings manager
    SharedPreferences settings;
    SharedPreferences.Editor settingsHandler;

    String token = "";
    String userid = "";
    String pword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_stars_web_page);

        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        // load the token
        token = settings.getString("token", "");
        userid = settings.getString("user", "");
        pword = settings.getString("password", "");

        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new PeerStarsWebClient());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        //webView.loadUrl("http://wcmsoftwareinc.ddns.net:25888/PeerStars");
        //webView.loadUrl("http://wcmsoftwareinc.ddns.net:25888/PeerStars/MobileFlipBook.jsp?userid=" + userid + "&password=" + pword);
        //webView.loadUrl("http://wcmsoftware.ddns.net:25888/PeerStars/AppLogin.jsp?userid=" + userid + "&password=" + pword);
        webView.loadUrl("http://wcmsoftware.ddns.net:26888/PeerStars/AppLogin.jsp?userid=" + userid + "&password=" + pword);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_peer_stars_web_page, menu);
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

    public class PeerStarsWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub

            view.loadUrl(url);
            return true;

        }
    }

    // To handle "Back" key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onClick_Return(View v) {
        finish();
    }
}
