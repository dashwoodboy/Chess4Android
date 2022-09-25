package pt.isel.pdm.chess4android.localgame

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.chess.*
import pt.isel.pdm.chess4android.databinding.ActivityLocalGameBinding

class LocalGameActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLocalGameBinding.inflate(layoutInflater)
    }

    private val viewModel: LocalGameActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.boardLiveData.observe(this) { board ->
            if (board != null)
                binding.boardView.setBoard(board)

            if (binding.toggleAutoFlipButton.isChecked)
                binding.boardView.flip(viewModel.chessGame.pov)
        }

        binding.boardView.flip(viewModel.chessGame.pov)

        binding.boardView.onTileClickedListener = { row: Int, column: Int ->
            if (viewModel.selectSquare(row, column)) {

                if (viewModel.chessGame.isPromoting)
                    enablePromotionButtons()

                checkIfGameOver()
                checkNavigationButtons()
            }
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

        binding.drawButton.setOnClickListener { viewModel.chessGame.drawAgreed(); checkIfGameOver() }
        binding.resignButton.setOnClickListener { viewModel.chessGame.resign(); checkIfGameOver() }

        binding.toggleAutoFlipButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                binding.boardView.flip(viewModel.chessGame.pov)
        }

        disablePromotionButtons()

        binding.queenButton.setOnClickListener { viewModel.promote(Queen::class.java); disablePromotionButtons() }
        binding.rookButton.setOnClickListener { viewModel.promote(Rook::class.java); disablePromotionButtons() }
        binding.bishopButton.setOnClickListener { viewModel.promote(Bishop::class.java); disablePromotionButtons() }
        binding.knightButton.setOnClickListener { viewModel.promote(Knight::class.java); disablePromotionButtons() }
    }

    private fun checkNavigationButtons() {
        val canCheckBack = viewModel.chessGame.canCheckPreviousMove()
        binding.moveBackButton.isEnabled = canCheckBack
        binding.moveBackButton.isClickable = canCheckBack

        val canCheckForward = viewModel.chessGame.canCheckForwardMove()
        binding.moveForwardButton.isEnabled = canCheckForward
        binding.moveForwardButton.isClickable = canCheckForward
    }

    private fun checkIfGameOver() {
        if (viewModel.chessGame.state != ChessGame.State.Playing) {
            disableEndingButtons()
            val message = when (viewModel.chessGame.state){
                ChessGame.State.BlackByCheckmate-> getString(R.string.black_win_text)
                ChessGame.State.WhiteByCheckmate-> getString(R.string.white_win_text)
                ChessGame.State.Stalemate -> getString(R.string.stalemate_text)
                ChessGame.State.DrawAgreed -> getString(R.string.draw_agreed_text)
                ChessGame.State.WhiteResigns -> getString(R.string.white_resigns_text)
                ChessGame.State.BlackResigns -> getString(R.string.black_resigns_text)
                else -> getString(R.string.draw_text)
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableEndingButtons() {
        binding.drawButton.visibility = Button.INVISIBLE
        binding.resignButton.visibility = Button.INVISIBLE
    }

    private fun disablePromotionButtons() {
        binding.queenButton.visibility = Button.INVISIBLE
        binding.rookButton.visibility = Button.INVISIBLE
        binding.bishopButton.visibility = Button.INVISIBLE
        binding.knightButton.visibility = Button.INVISIBLE
    }

    private fun enablePromotionButtons() {
        binding.queenButton.visibility = Button.VISIBLE
        binding.rookButton.visibility = Button.VISIBLE
        binding.bishopButton.visibility = Button.VISIBLE
        binding.knightButton.visibility = Button.VISIBLE
    }
}