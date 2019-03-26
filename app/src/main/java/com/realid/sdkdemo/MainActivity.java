package com.realid.sdkdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.FileProvider.getUriForFile;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_TAKE_VIDEO = 2;
    private static final String FILE_TYPE_PHOTO = "image/*";
    private static final String FILE_TYPE_VIDEO = "video/*";


    private ValueCallback<Uri[]> fileCallBack;

    private String photoPath;
    private String videoPath;
    private String fileChooseType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createUI();
        initWebView();
        initWebViewClient();
        mWebView.requestFocus();
        mWebView.loadUrl("http://192.168.1.162:8903/sdk-front/");


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            webViewGo();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }


    private void webViewGo() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            this.finish();
        }
    }

    private void createUI() {
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.webView);
    }

    @SuppressLint("NewApi")
    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    private void initWebViewClient() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && url.contains("@")) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });


        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, QuotaUpdater quotaUpdater) {
                quotaUpdater.updateQuota(spaceNeeded * 2);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            @Override
            public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota,
                                                QuotaUpdater quotaUpdater) {
                quotaUpdater.updateQuota(estimatedDatabaseSize * 2);
            }

            @TargetApi(21)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                MainActivity.this.fileCallBack = filePathCallback;
                String[] acceptTypes = fileChooserParams.getAcceptTypes();
                if (null != acceptTypes && acceptTypes.length > 0) {
                    String currentTypes = acceptTypes[0];
                    fileChooseType = currentTypes;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        verifyCameraPermissions();
                    } else {
                        gotoTake();
                    }
                }
                return true;
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            if (resultCode == RESULT_CANCELED) {
                if (null != fileCallBack) {
                    fileCallBack.onReceiveValue(null);
                }
            }
            return;
        }

        if (null != fileCallBack) {
            Uri uri = null;
            if (requestCode == REQUEST_TAKE_PHOTO) {
                uri = Uri.fromFile(new File(photoPath));
            } else if (requestCode == REQUEST_TAKE_VIDEO) {
                uri = Uri.fromFile(new File(videoPath));
            }
            if (null != uri) {
                fileCallBack.onReceiveValue(new Uri[]{uri});
                fileCallBack = null;
            } else {
                fileCallBack.onReceiveValue(null);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            try {
                mWebView.removeAllViews();
                mWebView.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void verifyCameraPermissions() {
        List<String> permissions = null;
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions = new ArrayList<>();
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (this.checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (permissions == null) {
                permissions = new ArrayList<>();
            }
            permissions.add(Manifest.permission.CAMERA);
        }
        if (permissions == null) {
            gotoTake();
        } else {
            String[] permissionArray = new String[permissions.size()];
            permissions.toArray(permissionArray);
            requestPermissions(permissionArray, 0);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (permission.equals(Manifest.permission.CAMERA)) {
                    gotoTake();
                }
            }
        }
    }

    private void gotoTake() {
        if (FILE_TYPE_PHOTO.equals(fileChooseType)) {
            gotoTakePhoto();
        } else if (FILE_TYPE_VIDEO.equals(fileChooseType)) {
            gotoTakeVideo();
        }
    }

    private void gotoTakePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        photoFile = new File(photoFile, System.currentTimeMillis() + ".jpg");
        photoPath = photoFile.getAbsolutePath();
        if (Build.VERSION.SDK_INT > 23) {
            String authority = this.getApplicationContext().getPackageName() + ".fileProvider";
            Uri contentUri = getUriForFile(this, authority, photoFile);
            this.grantUriPermission(this.getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        }
        this.startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void gotoTakeVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3);
        File videoFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        videoFile = new File(videoFile, System.currentTimeMillis() + ".mp4");
        videoPath = videoFile.getAbsolutePath();
        if (Build.VERSION.SDK_INT > 23) {
            String authority = this.getApplicationContext().getPackageName() + ".fileProvider";
            Uri contentUri = getUriForFile(this, authority, videoFile);
            this.grantUriPermission(this.getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
        }
        this.startActivityForResult(intent, REQUEST_TAKE_VIDEO);
    }

}
