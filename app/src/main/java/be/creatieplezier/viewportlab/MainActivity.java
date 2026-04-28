package be.creatieplezier.viewportlab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static final int LOCAL_PORT = 53847;
    public static final String LOCAL_URL = "http://127.0.0.1:" + LOCAL_PORT + "/";

    private LocalAssetHttpServer localServer;
    private WebView webView;
    private FrameLayout root;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(109, 57, 242));
        getWindow().setNavigationBarColor(Color.rgb(77, 35, 199));

        root = new FrameLayout(this);
        setContentView(root);

        showLoadingView();
        startLocalServer();
        setupWebView();

        mainHandler.postDelayed(() -> webView.loadUrl(LOCAL_URL), 250);
    }

    private void showLoadingView() {
        TextView loading = new TextView(this);
        loading.setText("Bootstrap Viewport Lab Pro\n\nLocalhost starten op 127.0.0.1:" + LOCAL_PORT + " ...");
        loading.setTextColor(Color.rgb(6, 24, 74));
        loading.setTextSize(18f);
        loading.setGravity(Gravity.CENTER);
        loading.setBackgroundColor(Color.rgb(244, 245, 247));

        root.addView(loading, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void startLocalServer() {
        try {
            localServer = new LocalAssetHttpServer(this, LOCAL_PORT, "www");
            localServer.start();
        } catch (Exception exception) {
            Toast.makeText(this, "Localhost kon niet starten: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new ViewportLabWebViewClient());

        root.removeAllViews();
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private class ViewportLabWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            String url = uri.toString();

            if (url.startsWith(LOCAL_URL) || url.startsWith("http://127.0.0.1:" + LOCAL_PORT) || url.startsWith("about:")) {
                return false;
            }

            // Laat iframe-content in de WebView laden. Alleen top-level externe navigatie openen we extern.
            if (request.isForMainFrame()) {
                openExternal(url);
                return true;
            }

            return false;
        }
    }

    private void openExternal(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, "Geen app gevonden om deze URL te openen.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        if (localServer != null) {
            localServer.stop();
            localServer = null;
        }

        super.onDestroy();
    }
}
