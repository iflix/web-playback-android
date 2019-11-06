package iflix.play.webview.player

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.support.v4.content.ContextCompat.startActivity




class IflixPlayerWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var webChromeClient: VideoEnabledWebChromeClient

    companion object {
        val INTENT_IFLIX_ASSET_TYPE = "asset_type"
        val INTENT_IFLIX_ASSET_ID = "asset_id"

        val IFLIX_ASSET_TYPE_MOVIE = "movie"
        val IFLIX_ASSET_TYPE_SHOW = "show"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // force
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        setContentView(R.layout.activity_webview)

        webView = findViewById(R.id.webView)

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        val nonVideoLayout = findViewById<View>(R.id.nonVideoLayout)
        val videoLayout = findViewById<ViewGroup>(R.id.videoLayout)

        setTitle("")

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

        // include "partner/grab" in the user agent string for tracking
        settings.userAgentString = System.getProperty("http.agent") + " partner/webtest"

        // WebView settings
        webView.fitsSystemWindows = true

        webView.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.contains("/embed") && url.contains("iflix.com")) {
                    view.loadUrl(url)
                } else {
                    view.getContext()?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                return true
            }
        }

        webChromeClient = object: VideoEnabledWebChromeClient(nonVideoLayout, videoLayout) {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest?) {
                request!!.grant(arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID))
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                view?.getContext()?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(view.getHitTestResult().extra)))
                return false
            }
        }

        // force fullscreen
        window.decorView.makeFullscreen()

        webView.webChromeClient = webChromeClient

        // load the intent params
        val assetType = intent.getStringExtra(INTENT_IFLIX_ASSET_TYPE)
        val assetId = intent.getStringExtra(INTENT_IFLIX_ASSET_ID)

        val url = "https://www.iflix.com/embed/short/112774"
        webView.loadUrl(url)
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

// making views full screen or non-immersive full screen
fun View.makeFullscreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        this.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    } else {
        makeNonImmersiveFullscreen()
    }
}

fun View.makeNonImmersiveFullscreen() {
    this.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)
}

