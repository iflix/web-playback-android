package iflix.play.webview.example

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import iflix.play.webview.player.IflixPlayerWebViewActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button1).setOnClickListener {
            startUrl("https://www.iflix.com/embed/short/112774")
        }

        findViewById<Button>(R.id.button2).setOnClickListener {
            startUrl("https://www.iflix.com/embed/movie/256412")
        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            startUrl("https://www.iflix.com/embed/movie/250396")
        }

        findViewById<Button>(R.id.button4).setOnClickListener {
            startUrl("https://www.iflix.com/embed/episode/118473")
        }

        findViewById<Button>(R.id.button5).setOnClickListener {
            startUrl("https://www.iflix.com/embed/episode/118474")
        }
    }

    private fun startUrl(url: String) {
        val intent = Intent(this, IflixPlayerWebViewActivity::class.java)
        intent.putExtra(IflixPlayerWebViewActivity.INTENT_IFLIX_ASSET_URL, url)
        startActivity(intent)
    }
}
