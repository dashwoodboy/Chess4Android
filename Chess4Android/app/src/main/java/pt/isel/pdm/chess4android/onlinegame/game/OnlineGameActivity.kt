package pt.isel.pdm.chess4android.onlinegame.game

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.chess.*
import pt.isel.pdm.chess4android.databinding.ActivityOnlineGameBinding

private const val GAME_STATE = "GameState"
private const val PLAYER_COLOR = "PlayerColor"

class OnlineGameActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityOnlineGameBinding.inflate(layoutInflater)
    }

    /**
     * Obtains gameState extra from intent,
     * may throw an error if there is no extra corresponding to[GAME_STATE]
     */
    private val initialState: GameState by lazy {
        intent.getParcelableExtra<GameState>(GAME_STATE)
            ?: throw IllegalArgumentException("Mandatory extra gameState not present")
    }

    /**
     * Obtains gameState extra from intent,
     * may throw an error if there is no extra corresponding to[PLAYER_COLOR]
     */
    private val playerColor: Army by lazy {
        val ordinal = intent.getIntExtra(PLAYER_COLOR, -1)
        if (ordinal == -1)
            throw java.lang.IllegalArgumentException("Mandatory extra playerColor not present")
        Army.values()[ordinal]
    }

    /**
     * Obtains ViewModel by viewModels overriding Factory's create function to allow
     * a constructor with initialState(instance of [GameState])
     * and playerColor(instance of [PLAYER_COLOR]
     */
    private val viewModel: OnlineGameActivityViewModel by viewModels {
        @Suppress("UNCHECKED_CAST")
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OnlineGameActivityViewModel(application, initialState, playerColor) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.boardLiveData.observe(this) { board ->
            if (board != null)
                binding.boardView.setBoard(board)
        }

        viewModel.stateLiveData.observe(this) {
            if (it != null) {
                checkIfGameOver()
            }
        }

        viewModel.drawRequestLiveData.observe(this) {
            if (it != null) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.propose_draw_text))
                    .setPositiveButton(getString(R.string.accept_draw_text)) { _, _ ->
                        viewModel.responseDraw(
                            true
                        )
                    }
                    .setNegativeButton(getString(R.string.decline_draw_text)) { _, _ ->
                        viewModel.responseDraw(
                            false
                        )
                    }
                    .create()
                    .show()
            }
        }

        binding.boardView.flip(viewModel.chessGame.pov)

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

        binding.boardView.onTileClickedListener = { row: Int, column: Int ->
            if (viewModel.selectSquare(row, column)) {

                if (viewModel.chessGame.isPromoting)
                    enablePromotionButtons()

                checkIfGameOver()
                checkNavigationButtons()
            }
        }

        binding.resignButton.setOnClickListener {
            viewModel.resign()
        }

        binding.drawButton.setOnClickListener {
            viewModel.requestDraw()
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

    /**
     *
     * Verifies the occurrence of game over and shows a toast with the respective message
     */
    private fun checkIfGameOver() {
        if (viewModel.chessGame.state != ChessGame.State.Playing) {
            disableEndingButtons()
            val message = when (viewModel.chessGame.state) {
                ChessGame.State.BlackByCheckmate -> getString(R.string.black_win_text)
                ChessGame.State.WhiteByCheckmate -> getString(R.string.white_win_text)
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

    /**
     * Disable visibility of promotion buttons
     */
    private fun disablePromotionButtons() {
        binding.queenButton.visibility = Button.INVISIBLE
        binding.rookButton.visibility = Button.INVISIBLE
        binding.bishopButton.visibility = Button.INVISIBLE
        binding.knightButton.visibility = Button.INVISIBLE
    }

    /**
     * Enables visibility of promotion buttons
     */
    private fun enablePromotionButtons() {
        binding.queenButton.visibility = Button.VISIBLE
        binding.rookButton.visibility = Button.VISIBLE
        binding.bishopButton.visibility = Button.VISIBLE
        binding.knightButton.visibility = Button.VISIBLE
    }
}