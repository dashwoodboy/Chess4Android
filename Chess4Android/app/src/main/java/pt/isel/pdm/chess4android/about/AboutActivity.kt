package pt.isel.pdm.chess4android.about

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import pt.isel.pdm.chess4android.R

class AboutActivity : AppCompatActivity() {

    private val lichessURL = "https://lichess.org/api/puzzle/daily"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<ImageView>(R.id.lichess_icon).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lichessURL)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }
}