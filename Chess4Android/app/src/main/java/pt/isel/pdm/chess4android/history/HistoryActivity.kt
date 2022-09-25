package pt.isel.pdm.chess4android.history

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isel.pdm.chess4android.daily.DailyPuzzleActivity
import pt.isel.pdm.chess4android.databinding.ActivityHistoryBinding

private const val PUZZLE_OF_DAY = "PuzzleOfDay"
private const val SHOWCASE = "Showcase"
private const val DATE = "Date"

/**
 * The screen used to display the list of daily puzzles stored locally.
 */
class HistoryActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityHistoryBinding.inflate(layoutInflater)
    }

    private val viewModel: HistoryActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.puzzleList.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        updatePuzzleList()
    }

    /**
     * Update the puzzle list.
     */
    private fun updatePuzzleList() {
        viewModel.getHistory().observe(this) { DTOS ->
            binding.puzzleList.adapter = HistoryAdapter(DTOS) {
                val intent = Intent(this, DailyPuzzleActivity::class.java)
                intent.putExtra(PUZZLE_OF_DAY, it.puzzle)
                if (it.completed == 0) {
                    intent.putExtra(SHOWCASE, false)
                    intent.putExtra(DATE, it.date)
                } else intent.putExtra(SHOWCASE, true)
                startActivity(intent)
            }
        }
    }
}