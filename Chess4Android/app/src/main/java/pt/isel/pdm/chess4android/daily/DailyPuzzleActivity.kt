package pt.isel.pdm.chess4android.daily

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.common.PuzzleOfDay
import pt.isel.pdm.chess4android.databinding.ActivityDailyPuzzleBinding

private const val PUZZLE_OF_DAY = "PuzzleOfDay"
private const val SHOWCASE = "Showcase"
private const val DATE = "Date"

class DailyPuzzleActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDailyPuzzleBinding.inflate(layoutInflater)
    }

    private val viewModel: DailyPuzzleActivityViewModel by viewModels()

    private var isShowcase: Boolean = false
    private var date: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val puzzleOfDay: PuzzleOfDay = intent.extras?.get(PUZZLE_OF_DAY) as PuzzleOfDay

        viewModel.start(puzzleOfDay)
        binding.boardView.flip(viewModel.chessPuzzle.pov)

        viewModel.boardLiveData.observe(this) { board ->
            if (board != null)
                binding.boardView.setBoard(board)
        }

        binding.flipBoardButton.setOnClickListener {
            binding.boardView.flip(viewModel.changePov())
        }

        checkNavigationButtons()

        binding.moveBackButton.setOnClickListener {
            viewModel.moveBack()
            checkNavigationButtons()
        }
        binding.moveForwardButton.setOnClickListener {
            viewModel.moveForward()
            checkNavigationButtons()
        }

        isShowcase = intent.extras?.getBoolean(SHOWCASE) as Boolean
        date = intent.extras?.getString(DATE)

        if (!isShowcase) {
            enablePuzzle()
        } else {
            binding.playPuzzleButton.setOnClickListener {
                enablePuzzle()
            }

            binding.showSolutionButton.setOnClickListener {
                disableAndHideButton(binding.playPuzzleButton)
                if (viewModel.nextMove()) {
                    disableAndHideButton(binding.showSolutionButton)
                    Toast.makeText(this, getText(R.string.end_of_solution), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkNavigationButtons() {
        val canCheckBack = viewModel.chessPuzzle.canCheckPreviousMove()
        binding.moveBackButton.isEnabled = canCheckBack
        binding.moveBackButton.isClickable = canCheckBack

        val canCheckForward = viewModel.chessPuzzle.canCheckForwardMove()
        binding.moveForwardButton.isEnabled = canCheckForward
        binding.moveForwardButton.isClickable = canCheckForward
    }

    private fun disableAndHideButton(button: Button) {
        button.isEnabled = false
        button.isInvisible = true
    }

    private fun enablePuzzle() {
        disableAndHideButton(binding.playPuzzleButton)
        disableAndHideButton(binding.showSolutionButton)

        binding.boardView.onTileClickedListener = { row: Int, column: Int ->
            if (viewModel.selectSquare(row, column)) {
                if (viewModel.chessPuzzle.moved) {
                    if (viewModel.chessPuzzle.wonPuzzle) {
                        if (!isShowcase) {
                            viewModel.setCompleted(date as String)
                        }
                        Toast.makeText(this, getText(R.string.won_puzzle), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, getText(R.string.lost_puzzle), Toast.LENGTH_SHORT)
                        .show()
                }
                checkNavigationButtons()
            }
        }
    }
}