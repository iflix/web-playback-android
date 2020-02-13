package iflix.play.webview.player

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.*
import android.widget.Button
import android.widget.TextView


class IflixPlayerWebViewActivity : AppCompatActivity() {

    private var playing: Boolean = false
    private lateinit var magicButton: Button
    private lateinit var debugView: TextView
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
        debugView = findViewById(R.id.debug_view)
        magicButton = findViewById(R.id.magic_button)
        magicButton.setOnClickListener {
            Log.d("MagicButtonDispatch", "MagicButtonDispatch sending " + if (playing) "pause event" else "play event")
            webView.evaluateJavascript(if (playing) {
                "window.dispatchEvent(new Event('video-pause'));"
            } else {
                "window.dispatchEvent(new Event('video-play'));"
            }) { Log.d("MagicButtonDispatch", "MagicButtonDispatch Result: $it")}
        }

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
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setUpEventListeners(view ?: return)
            }

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

        val url = "https://www.iflix.com/embed/$assetType/$assetId"
        webView.loadUrl(url)
        bindJavascriptInterface(webView)
    }

    private fun updateButtonState() {
        if (playing) {
            magicButton.text = "Pause"
        } else {
            magicButton.text = "Play"
        }
    }

    private fun setUpEventListeners(webView: WebView) {
        webView.evaluateJavascript("""
            window.addEventListener('video-isLoaded', function () { iflixCallbacks.isLoaded() });
            window.addEventListener('video-isLoading', function () { iflixCallbacks.isLoading() });
            window.addEventListener('video-isPlaying', function () { iflixCallbacks.isPlaying() });
            window.addEventListener('video-isPaused', function () { iflixCallbacks.isPaused() });
        """) {}
    }

    private fun bindJavascriptInterface(webView: WebView) {
        class IflixJavascriptInterface(private val context: Context) {
            @JavascriptInterface
            fun isLoaded() {
                runOnUiThread {
                    debugView.text = "State: Loaded"
                    updateButtonState()
                }
            }

            @JavascriptInterface
            fun isLoading() {
                runOnUiThread {
                    debugView.text = "State: Loading"
                }
            }

            @JavascriptInterface
            fun isPlaying() {
                runOnUiThread {
                    debugView.text = "State: Playing"
                    playing = true
                    updateButtonState()
                }
            }

            @JavascriptInterface
            fun isPaused() {
                runOnUiThread {
                    debugView.text = "State: Paused"
                    playing = false
                    updateButtonState()
                }
            }
        }
        webView.addJavascriptInterface(IflixJavascriptInterface(this), "iflixCallbacks")
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

