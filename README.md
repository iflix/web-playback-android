## Android Web View Playback

### Minimum Requirements

The following details the requirements for embedding webv3 within a webview for an Android app, supporting video playback of DRM encrypted streams.

In order for video playback to work within a webview container, the following minimum requirements must be met

  * Android Lollipop and above - this is required because EME with Widevine DRM encryption is only supported in this version and later. 
  * The application containing the webview must contain some modifications to automatically accept DRM prompts that would otherwise occur in the web browser. See [WebChromeClient](http://developer.android.com/reference/android/webkit/WebChromeClient.html#onPermissionRequest)
  * Example code acheiving the above
  ```
  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  override fun onPermissionRequest(request: PermissionRequest?) {
     request!!.grant(arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID))
  }
  ```
  * This is required as iflix streams require Widevine L1.
  * Some modifications may be required to the webview to support full screen

---

### Import our reference library


For following library contains the iflix Android WebView Player library and example code. It solves for all of the above requirements.

To use the web view player follow the following.


#### 1 - Import the Player library AAR

Place the AAR file in your apps `lib` directory and import it into the project
`iflix-webplayer-release-v1.0.0.aar`


#### 2 - Load the iflix asset data from our API

In your app load and present the iflix asset data to your users. Directions for API usage are at [developer.iflix.com](http://developer.iflix.com)


#### 3 - Start the Player

Start the `IflixPlayerWebViewActivity` passing in the values for `INTENT_IFLIX_ASSET_TYPE` and `INTENT_IFLIX_ASSET_ID`.

`INTENT_IFLIX_ASSET_TYPE` must equal `IFLIX_ASSET_TYPE_MOVIE` OR `IFLIX_ASSET_TYPE_SHOW`. You can determine this from our API.

`INTENT_IFLIX_ASSET_ID` must be the asset ID of the movie or show.

Example code in Kotlin

```
val intent = Intent(this, IflixPlayerWebViewActivity::class.java)
intent.putExtra(INTENT_IFLIX_ASSET_TYPE, IFLIX_ASSET_TYPE_MOVIE) // IFLIX_ASSET_TYPE_MOVIE or IFLIX_ASSET_TYPE_SHOW
intent.putExtra(INTENT_IFLIX_ASSET_ID, "128530") // wheely movie id
startActivity(intent)
```

Example code is provided in the project as well as source code for the `IflixWebViewPlayerActivity`
