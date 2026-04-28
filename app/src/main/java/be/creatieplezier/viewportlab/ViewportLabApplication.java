package be.creatieplezier.viewportlab;

import android.app.Application;
import android.webkit.WebView;

public class ViewportLabApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}
