package com.rcumis.vigilance.vicpl;


import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.rcumis.vigilance.vicpl.location.BackgroundLocationService;

/**
 * Created by Nagarjuna on 23/10/2016
 */
public class MainActivity extends AppCompatActivity {

    private final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    public static final int PERMISSION_STORAGE_STOPS_REQUEST_CODE = 122;
    private WebView mWebView;
    private Dialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWebView();
        startLocationService();
    }

    private void initProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new Dialog(this, R.style.CustomDialogAnimTheme);
        }
        mProgressDialog.setContentView(R.layout.progress_bar);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    public void showProgressDialog() {

                try {
                    if (mProgressDialog == null)
                        initProgressDialog();
                    if (!mProgressDialog.isShowing())
                        mProgressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }


    public void hideProgressDialog() {
                try {
                    if (mProgressDialog != null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mProgressDialog = null;
                }
    }

    private void initWebView() {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.setWebViewClient(new MyWebClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl("http://vigilance.rcumis.com");
    }

    private void startLocationService() {
        ComponentName comp = new ComponentName(getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service = startService(new Intent().setComponent(comp));

        if (null == service){
            // something really wrong here
            Log.e("MainActivity", "Could not start service " + comp.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the permissions are already granted or not
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No permission granted, request the permission(s).
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults != null && grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    // Hence get the location updates(for Android-M devices)
                    //Here we have to check for the read and write external storage permissions
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // No permission granted, request the permission(s).
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_STORAGE_STOPS_REQUEST_CODE);
                        // The callback method gets the result of the request.
                    }
                    startLocationService();

                }
                return;
            }
            case PERMISSION_STORAGE_STOPS_REQUEST_CODE: {
                startLocationService();
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
            }
            break;
        }
    }

    public class MyWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showProgressDialog();
            Log.i("requested URL : ", url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hideProgressDialog();
        }

        @Override
        public void onLoadResource(WebView view, String url) {

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            super.onFormResubmission(view, dontResend, resend);
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
        }
    }
}
