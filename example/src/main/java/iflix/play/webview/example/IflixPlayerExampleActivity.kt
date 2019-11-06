package iflix.play.webview.example

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import iflix.play.webview.player.IflixPlayerWebViewActivity
import iflix.play.webview.player.IflixPlayerWebViewActivity.Companion.IFLIX_ASSET_TYPE_MOVIE
import iflix.play.webview.player.IflixPlayerWebViewActivity.Companion.IFLIX_ASSET_TYPE_SHOW
import iflix.play.webview.player.IflixPlayerWebViewActivity.Companion.INTENT_IFLIX_ASSET_ID
import iflix.play.webview.player.IflixPlayerWebViewActivity.Companion.INTENT_IFLIX_ASSET_TYPE

class IflixPlayerExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, IflixPlayerWebViewActivity::class.java)
        intent.putExtra(INTENT_IFLIX_ASSET_TYPE, IFLIX_ASSET_TYPE_SHOW) // IFLIX_ASSET_TYPE_MOVIE or IFLIX_ASSET_TYPE_SHOW
        intent.putExtra(INTENT_IFLIX_ASSET_ID, "18808") // wheely movie id
        startActivity(intent)

        finish()
    }

}
