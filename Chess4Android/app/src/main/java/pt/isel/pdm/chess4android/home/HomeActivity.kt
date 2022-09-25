package pt.isel.pdm.chess4android.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import pt.isel.pdm.chess4android.daily.DailyPuzzleActivity
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.about.AboutActivity
import pt.isel.pdm.chess4android.common.getCurrentDate
import pt.isel.pdm.chess4android.databinding.ActivityHomeBinding
import pt.isel.pdm.chess4android.history.HistoryActivity
import pt.isel.pdm.chess4android.localgame.LocalGameActivity
import pt.isel.pdm.chess4android.onlinegame.challenge.list.ChallengesListActivity

private const val PUZZLE_OF_DAY = "PuzzleOfDay"
private const val SHOWCASE = "Showcase"
private const val DATE = "Date"

class HomeActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private val viewModel: HomeActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.fetchPuzzleButton.setOnClickListener {view->
            view.isEnabled = false
            viewModel.getPuzzleOfDay().observe(this){
                if (it != null) {
                    val intent = Intent(this, DailyPuzzleActivity::class.java)
                    intent.putExtra(PUZZLE_OF_DAY, it)
                    intent.putExtra(SHOWCASE, false)
                    intent.putExtra(DATE, getCurrentDate())
                    startActivity(intent)
                    view.isEnabled = true
                } else {
                    Toast
                        .makeText(this,getText(R.string.internet_connection), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.localButton.setOnClickListener {
            it.isEnabled = false
            val intent = Intent(this, LocalGameActivity::class.java)
            startActivity(intent)
            it.isEnabled = true
        }

        binding.onlineButton.setOnClickListener {
            it.isEnabled = false
            startActivity(Intent(this, ChallengesListActivity::class.java))
            it.isEnabled = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.credits -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}