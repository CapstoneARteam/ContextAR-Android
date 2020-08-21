package com.example.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity
{
        public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 10.0.0; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";

        final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
        private int webViewPreviousState;
        private final int PAGE_STARTED = 0x1;
        private final int PAGE_REDIRECTED = 0x2;
        private WebView webView;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                webView = (WebView) findViewById(R.id.activity_main_webview);

                if (Build.VERSION.SDK_INT >= 23) {
                        // Marshmallow+ Permission APIs
                        handleMarshMallow();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                                WebView.setWebContentsDebuggingEnabled(true);
                        }
                }
                webView.setInitialScale(1);
                webView.getSettings().setLoadWithOverviewMode(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
                webView.setScrollbarFadingEnabled(false);

                webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.setWebViewClient(new GeoWebViewClient());
                // Below required for geolocation
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setGeolocationEnabled(true);
                webView.setWebChromeClient(new GeoWebChromeClient());
                webView.getSettings().setUserAgentString(USER_AGENT);

                webView.getSettings().setAppCacheEnabled(true);
                webView.getSettings().setDatabaseEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);

                webView.getSettings().setAppCacheMaxSize( 5 * 1024 * 1024 );
                webView.getSettings().setAppCachePath( MainActivity.this.getApplicationContext().getCacheDir().getAbsolutePath() );
                webView.getSettings().setAllowFileAccess( true );
                webView.getSettings().setAppCacheEnabled( true );
                webView.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT );
                if ( !isInternetAvailable() ) { // loading offline
                        webView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
                }

                webView.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());


                if(savedInstanceState!=null) {
                        webView.restoreState(savedInstanceState);
                        webView.loadUrl(getPrefrenceBundle());
                }
                else {

                        webView.loadUrl("https://capstonearteam.github.io");
                }
        }

        /**
         * WebChromeClient subclass handles UI-related calls
         * Note: think chrome as in decoration, not the Chrome browser
         */
        public class GeoWebChromeClient extends android.webkit.WebChromeClient {
                @Override
                public void onGeolocationPermissionsShowPrompt(final String origin,
                                                               final GeolocationPermissions.Callback callback) {
                        // Always grant permission since the app itself requires location
                        // permission and the user has therefore already granted it
                        callback.invoke(origin, true, false);


                }
        }

        /**
         * WebViewClient subclass loads all hyperlinks in the existing WebView
         */
        public class GeoWebViewClient extends WebViewClient {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        // When user clicks a hyperlink, load in the existing WebView
                        view.loadUrl(url);
                        return true;
                }

                Dialog loadingDialog = new Dialog(MainActivity.this);

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        webViewPreviousState = PAGE_STARTED;

                        if (loadingDialog == null || !loadingDialog.isShowing())
                                loadingDialog = ProgressDialog.show(MainActivity.this, "",
                                        "Loading Please Wait", true, true,
                                        new DialogInterface.OnCancelListener() {

                                                @Override
                                                public void onCancel(DialogInterface dialog) {
                                                        // do something
                                                }
                                        });

                        loadingDialog.setCancelable(false);
                }



                @Override
                public void onPageFinished(WebView view, String url) {

                        if (webViewPreviousState == PAGE_STARTED) {

                                if (null != loadingDialog) {
                                        loadingDialog.dismiss();
                                        loadingDialog = null;
                                }
                        }
                }
        }


        /**
         * Check if there is any connectivity
         *
         * @return is Device Connected
         */
        public boolean isConnected() {

                ConnectivityManager cm = (ConnectivityManager)
                        this.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (null != cm) {
                        NetworkInfo info = cm.getActiveNetworkInfo();
                        return (info != null && info.isConnected());
                }

                return false;

        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                switch (requestCode) {
                        case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                                Map<String, Integer> perms = new HashMap<String, Integer>();
                                // Initial
                                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);


                                // Fill with results
                                for (int i = 0; i < permissions.length; i++)
                                        perms.put(permissions[i], grantResults[i]);

                                // Check for ACCESS_FINE_LOCATION
                                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED


                                ) {
                                        // All Permissions Granted

                                        // Permission Denied
                                        Toast.makeText(MainActivity.this, "All Permission GRANTED ", Toast.LENGTH_SHORT)
                                                .show();

                                } else {
                                        // Permission Denied
                                        Toast.makeText(MainActivity.this, "One or More Permissions are DENIED Exiting App ", Toast.LENGTH_SHORT)
                                                .show();

                                        finish();
                                }
                        }
                        break;
                        default:
                                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
        }

        @TargetApi(Build.VERSION_CODES.M)
        private void handleMarshMallow() {
                List<String> permissionsNeeded = new ArrayList<String>();

                final List<String> permissionsList = new ArrayList<String>();
                if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                        permissionsNeeded.add("Show Location");

                if (permissionsList.size() > 0) {
                        if (permissionsNeeded.size() > 0) {

                                // Need Rationale
                                String message = "App need access to " + permissionsNeeded.get(0);

                                for (int i = 1; i < permissionsNeeded.size(); i++)
                                        message = message + ", " + permissionsNeeded.get(i);

                                showMessageOKCancel(message,
                                        new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                                }
                                        });
                                return;
                        }
                        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                        return;
                }

                Toast.makeText(MainActivity.this, "No new Permission Required", Toast.LENGTH_SHORT)
                        .show();
        }


        private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton("OK", okListener)
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
        }

        @TargetApi(Build.VERSION_CODES.M)
        private boolean addPermission(List<String> permissionsList, String permission) {

                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionsList.add(permission);
                        // Check for Rationale Option
                        if (!shouldShowRequestPermissionRationale(permission))
                                return false;
                }
                return true;
        }


        public boolean isInternetAvailable() {
                try {
                        InetAddress ipAddr = InetAddress.getByName("https://capstonearteam.github.io");
                        //You can replace it with your name
                        return !ipAddr.equals("");

                } catch (Exception e) {
                        return false;
                }
        }


        public void saveToPreferences(Bundle out)
        {
                SharedPreferences preferences = getSharedPreferences("AUTHENTICATION_FILE_NAME", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("url",out.toString());

                editor.apply();
        }
        public String getPrefrenceBundle()
        {
                SharedPreferences prfs = getSharedPreferences("AUTHENTICATION_FILE_NAME", Context.MODE_PRIVATE);
                return prfs.getString("url", "");
        }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
        @Override
        public void onPause() {
                super.onPause(); // To change body of overridden methods use File |
                // Settings | File Templates.
                String url = webView.getUrl();
                Bundle out = new Bundle();
                webView.saveState(out);
                out.putString("url", url);
                //save the bundle to shared prefrence
                saveToPreferences(out);

             //   webView.onPause();
        }

        @Override
        public void onResume() {
                super.onResume(); // To change body of overridden methods use File |
                // Settings | File Templates.
                webView.onResume();
        }

        @Override
        public void onStop() {
                super.onStop(); // To change body of overridden methods use File |
                // Settings | File Templates.
                String url = webView.getUrl();
                Bundle out = new Bundle();
                webView.saveState(out);
                out.putString("url", url);
                //save the bundle to shared prefrence
                saveToPreferences(out);
        }




        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
                super.onSaveInstanceState(savedInstanceState);
                webView.saveState(savedInstanceState);
        }

        @Override
        public void onRestoreInstanceState(Bundle outState) {
                super.onRestoreInstanceState(outState);
                webView.restoreState(outState);
        }


}



