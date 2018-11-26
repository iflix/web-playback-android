package iflix.play.webview.playback

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.View
import android.webkit.*
import android.webkit.PermissionRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.ViewGroup
import android.view.WindowManager
import iflix.play.webview.playback.R.id.webView


class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var webChromeClient: VideoEnabledWebChromeClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_webview)

        // Save the web view
        webView = findViewById(R.id.webView)

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        val nonVideoLayout = findViewById<View>(R.id.nonVideoLayout)
        val videoLayout = findViewById<ViewGroup>(R.id.videoLayout)


        setTitle("iflix webview test")

        // Get the web view settings instance
        val settings = webView.settings

        // Enable java script in web view
        settings.javaScriptEnabled = true

        // Enable and setup web view cache
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCachePath(cacheDir.path)

        // Enable disable images in web view
        settings.blockNetworkImage = false
        // Whether the WebView should load image resources
        settings.loadsImagesAutomatically = true

        // More web view settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true  // api 26
        }

        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.mediaPlaybackRequiresUserGesture = false

        // WebView settings
        webView.fitsSystemWindows = true

        webView.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        webChromeClient = object: VideoEnabledWebChromeClient(nonVideoLayout, videoLayout) {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest?) {
                request!!.grant(arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID))
            }
        }
        webChromeClient.setOnToggledFullscreen { fullscreen ->
            // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
            if (fullscreen) {
                val attrs = window.attributes
                attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
                attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                window.attributes = attrs
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
            } else {
                val attrs = window.attributes
                attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
                attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv()
                window.attributes = attrs
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }

        webView.webChromeClient = webChromeClient

        webView.loadUrl("https://m.iflix.com/movie/128530/play")
    }

    override fun onBackPressed() {
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                // Standard back button implementation (for example this could close the app)
                super.onBackPressed()
            }
        }
    }
}
