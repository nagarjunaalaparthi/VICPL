package com.rcumis.vigilance.vicpl;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
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

import java.io.File;
import java.net.URLDecoder;

/**
 * Created by Nagarjuna on 23/10/2016
 */
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, DownloadListener {

    private final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    public static final int PERMISSION_STORAGE_STOPS_REQUEST_CODE = 122;
    private WebView mWebView;
    private Dialog mProgressDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ValueCallback<Uri[]> mFilePathCallBack;
    private Uri mCapturedImageURI;
    public static final int FILE_CHOOSER_RESULT_CODE = 100;
    private File storageDir;
    private ValueCallback<Uri> mFileCallback;
    private boolean locationNeedToStart = false;

    private String MAIN_URL = "https://vigilance.rcumis.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        storageDir = new File(
                Environment.getExternalStorageDirectory(), "RCUMIS");

        if (!storageDir.exists()) {
            // Create AndroidExampleFolder at sdcard
            storageDir.mkdirs();
        }
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
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.setWebViewClient(new MyWebClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.loadUrl(MAIN_URL);
        mWebView.setDownloadListener(this);
    }

    private void startLocationService() {
        if (checkIsLocationIsEnabled()) {
            ComponentName comp = new ComponentName(getPackageName(), BackgroundLocationService.class.getName());
            ComponentName service = startService(new Intent().setComponent(comp));

            if (null == service) {
                // something really wrong here
                Log.e("MainActivity", "Could not start service " + comp.toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mWebView == null){
            initWebView();
        }
        checkIsLocationIsEnabled();
        checkAndStartService();

    }

    public boolean checkIsLocationIsEnabled() {
        LocationManager lm = null;
        boolean gps_enabled = false;
        if(lm==null)
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}

        if(!gps_enabled ){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(myIntent, LOCATION_PERMISSION_REQUEST_CODE);
                    //get gps
                }
            });
            dialog.setCancelable(false);
            dialog.show();

        }
        return gps_enabled;
    }


    public boolean checkForPermissions() {
        // Check if the permissions are already granted or not
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // No permission granted, request the permission(s).
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
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
                        if (locationNeedToStart) {
                            startLocationService();
                        }
                        // The callback method gets the result of the request.
                    }

                }
                return;
            }
            case PERMISSION_STORAGE_STOPS_REQUEST_CODE:
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

    @Override
    public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(s));
        request.setMimeType(s2);
        //------------------------COOKIE!!------------------------
        String cookies = CookieManager.getInstance().getCookie(s);
        request.addRequestHeader("cookie", cookies);
        //------------------------COOKIE!!------------------------
        request.addRequestHeader("User-Agent", s1);
        request.allowScanningByMediaScanner();
        request.setTitle(URLUtil.guessFileName(s, null, null));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
//        request.setDestinationInExternalFilesDir(this, Environment.getExternalStorageDirectory().getAbsolutePath(),getString(R.string.app_name));
        request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/RCUMIS/"+URLUtil.guessFileName(s, null, null))));
        if(s.lastIndexOf(".") != -1) {
            String ext = s.substring(s.lastIndexOf(".")+1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getMimeTypeFromExtension(ext);
            request.setMimeType(type);
        }
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //This is important!
        intent.addCategory(Intent.CATEGORY_OPENABLE); //CATEGORY.OPENABLE
        intent.setType("*/*");//any application,any extension
        Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                Toast.LENGTH_LONG).show();
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
                    VigilancePreferenceManager.setEmailOfuser(MainActivity.this, email);
                    checkAndStartService();
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

            if (url.equals(MAIN_URL)) {
                VigilancePreferenceManager.clear(MainActivity.this);
                stopLocationService();
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            checkAndStartService(url);
            return super.shouldInterceptRequest(view, url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//            if (request != null) {
//                String url = request.getUrl().toString();
//                checkAndStartService(url);
//            }
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

    private void checkAndStartService() {
//        if (url != null && url.contains("verifications/changeStatus")) {
//            Calendar past = Calendar.getInstance();
//            past.setTimeInMillis(VigilancePreferenceManager.getLaunchedTime(this));
//
//            if (past.before(Calendar.getInstance())) {
                if (VigilancePreferenceManager.getEmailOfUser(MainActivity.this).length() > 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            locationNeedToStart = true;
                            if (checkForPermissions()) {
                                startLocationService();
                            }
                        }
                    });
//                }
//            }
        }
    }

    private void stopLocationService() {
        locationNeedToStart = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopService(new Intent(MainActivity.this,BackgroundLocationService.class));
            }
        });
    }

    public class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress > 50){
                hideProgressDialog();
            }
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            super.onReceivedTouchIconUrl(view, url, precomposed);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView newWebView = new WebView(view.getContext());
            newWebView.getSettings().setJavaScriptEnabled(true);
            newWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            newWebView.getSettings().setSupportMultipleWindows(true);
            newWebView.getSettings().setLoadWithOverviewMode(true);
            newWebView.getSettings().setUseWideViewPort(true);
            newWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            newWebView.setWebViewClient(new PdfWebViewClient());
            newWebView.setWebChromeClient(new MyWebChromeClient());
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            resultMsg.getData();
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
//            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }


        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
//            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
        }

        //For Android 4.1 only
        protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
        {
            mFileCallback = uploadMsg;
            startChooser();
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg)
        {
            mFileCallback = uploadMsg;
            startChooser();
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mFileCallback = uploadMsg;
            startChooser();
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            mFilePathCallBack = filePathCallback;

           startChooser();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            Log.i("url_js_confirm",message+" url : "+url);
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.i("url_js_Alert",message+" url : "+url);
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            Log.i("url_js_prompt",message+" url : "+url);
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            Log.i("url_js_unload",message+" url : "+url);
            return super.onJsBeforeUnload(view, url, message, result);
        }
    }

    private void startChooser() {
        try{

            // Create AndroidExampleFolder at sdcard


            // Create camera captured image file path and name
            File file = new File(
                    storageDir + File.separator + "IMG_"
                            + String.valueOf(System.currentTimeMillis())
                            + ".jpg");

            mCapturedImageURI = Uri.fromFile(file);

            // Camera capture image intent
            final Intent captureIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");

            // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(i, "Upload Documents");

            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                    , new Parcelable[] { captureIntent });

            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);

        }
        catch(Exception e){
            Toast.makeText(getBaseContext(), "Exception:"+e,
                    Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (locationNeedToStart) {
                startLocationService();
            }
        }

        if(requestCode== FILE_CHOOSER_RESULT_CODE)
        {

            if (null == this.mFilePathCallBack) {
                return;

            }

            Uri[] result = null;

            try{
                if (resultCode != RESULT_OK) {

                    result = null;

                } else {

                    // retrieve from the private variable if the intent is null
                    if (data == null) {
                        result = new Uri[1];
                        result[0] = mCapturedImageURI;
                    } else if (data.getData() != null) {
                        result = new Uri[1];
                        result[0] = data.getData();
                    } else if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        result = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            result[i] = clipData.getItemAt(i).getUri();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                Toast.makeText(getApplicationContext(), "activity :"+e,
                        Toast.LENGTH_LONG).show();
            }

            if (result != null && result.length > 0){
                if (mFilePathCallBack != null) {
                    mFilePathCallBack.onReceiveValue(result);
                }
                if (mFileCallback != null) {
                    mFileCallback.onReceiveValue(result[0]);
                }
            } else {
                if (mFilePathCallBack != null) {
                    mFilePathCallBack.onReceiveValue(null);
                }
                if (mFileCallback != null) {
                    mFileCallback.onReceiveValue(null);
                }
            }
            mFilePathCallBack = null;
            mFileCallback = null;

        }
    }

    public class PdfWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            super.onPageStarted(view, url, favicon);
            mWebView.loadUrl(url);
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

