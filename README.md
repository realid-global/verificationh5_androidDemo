### Android invoke Guideline
#### 1.Manifest List Configuration
##### 1.1 Addition of permission

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

##### 1.2 Define provider

```
<provider
android:name="android.support.v4.content.FileProvider"
android:authorities="${applicationId}.fileProvider"
android:exported="false"
android:grantUriPermissions="true">
<meta-data
android:name="android.support.FILE_PROVIDER_PATHS"
android:resource="@xml/file_provider" />
</provider>

```
#### 2.Add resource file XML for provider resource
Note：please refer to demo for XML implementation method.


#### 3.Using camera and video in Webview
- Load the URL address provided by the SDK.
- WebView call setWebChromeClient() function.
- Override the WebChromeClient class the function onShowFileChooser（WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams）.
- Through fileChooserParams object invokes getAcceptTypes() function returns array of string.
- Through getAcceptTypes() function returns array ,then get the String by index=0 to identify if it is selfie capture or video.
- Use camera for selfie capture and video. Afterwhich through filePathCallback callback return the photo/video Uri{} object. Note: For implementation, please refer to Demo, MainActivity.