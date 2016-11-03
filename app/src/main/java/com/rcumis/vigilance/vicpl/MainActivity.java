package com.rcumis.vigilance.vicpl;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.rcumis.vigilance.vicpl.location.BackgroundLocationService;
import com.rcumis.vigilance.vicpl.network.NetworkUtils;
import com.rcumis.vigilance.vicpl.utils.VigilancePreferenceManager;

import java.net.URLDecoder;

/**
 * Created by Nagarjuna on 23/10/2016
 */
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    public static final int PERMISSION_STORAGE_STOPS_REQUEST_CODE = 122;
    private WebView mWebView;
    private Dialog mProgressDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        if (NetworkUtils.isNetworkAvailable(this)) {
            initWebView();
        } else {
            Toast.makeText(this,"No Network",Toast.LENGTH_LONG).show();
        }

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
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
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

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.setWebViewClient(new MyWebClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.loadUrl("https://vigilance.rcumis.com");
    }

    private void startLocationService() {
        ComponentName comp = new ComponentName(getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service = startService(new Intent().setComponent(comp));

        if (null == service){
            // something really wrong here
            Log.e("MainActivity", "Could not start service " + comp.toString());
        } else {
            Toast.makeText(this, "Location Service started", Toast.LENGTH_LONG ).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mWebView == null){
            initWebView();
        }
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

                }
                return;
            }
            case PERMISSION_STORAGE_STOPS_REQUEST_CODE: {
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
            }
            break;
        }
    }

    @Override
    public void onRefresh() {
        if (mWebView != null && mWebView.getUrl() != null){
            mWebView.loadUrl(mWebView.getUrl());
        } else {

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
            if (url.contains("email")) {
                    String urlEncoded = URLDecoder.decode(url);
                String[] emailArray = urlEncoded.split("/");
                String email = emailArray[emailArray.length-1];
                if (email != null && email.length() > 0) {
                    VigilancePreferenceManager.setEmialOfuser(MainActivity.this, email);
                }
                Log.i("requested email",email);
            }

            if (url.contains("verifications/verifSubmission")) {
                stopLocationService();
            }

            if (url.contains("dashboard")) {
            }

            if (url.contains("logout")) {
                stopLocationService();
            }

            Log.i("requested URLS: ", url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hideProgressDialog();
            Log.i("requested URLF: ", url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {

        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url != null && url.contains("verifications/changeStatus")) {
                if (VigilancePreferenceManager.getEmailOfUser(MainActivity.this).length() > 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startLocationService();
                        }
                    });
                }
            }
            return super.shouldInterceptRequest(view, url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request != null) {
                Log.i("requested URLW: ", request.getUrl().toString());
                String url = request.getUrl().toString();
                if (url != null && url.contains("verifications/changeStatus")) {
                    if (VigilancePreferenceManager.getEmailOfUser(MainActivity.this).length() > 0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startLocationService();
                            }
                        });
                    }
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            super.onFormResubmission(view, dontResend, resend);
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setPositiveButton(getString(R.string.continue_handler), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    handler.cancel();
                }
            });
            builder.setMessage(getString(R.string.ssl_error));
            builder.show();
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
        }
    }

    private void stopLocationService() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"Service stopped",Toast.LENGTH_LONG).show();
                stopService(new Intent(MainActivity.this,BackgroundLocationService.class));
            }
        });
    }

    public class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress > 75){
                hideProgressDialog();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

