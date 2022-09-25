package pt.isel.pdm.chess4android.localgame

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import pt.isel.pdm.chess4android.chess.Army
import pt.isel.pdm.chess4android.chess.ChessBoard
import pt.isel.pdm.chess4android.chess.ChessGame
import pt.isel.pdm.chess4android.chess.Element
import java.lang.reflect.Type

/**
 * The actual execution host behind the application's Local Game screen (i.e. the [LocalGameActivity]).
 */
class LocalGameActivityViewModel(
    application: Application,
    val state: SavedStateHandle
) : AndroidViewModel(application) {

    private val pgnKey = "PGN"

    val chessGame = ChessGame()

    /**
     * The [LiveData] instance used to publish the [ChessBoard].
     */
    val boardLiveData: MutableLiveData<MutableList<Element>> = MutableLiveData(chessGame.board)

    /**
     * If app was interrupted and there is a saved game on state,
     * this games is loaded; otherwise calls save's set function and saves current pgn.
     * Is also defined the point of view based on current player
     */
    init {
        val savedPgn = state.get<MutableList<String>>(pgnKey)
        if (savedPgn == null) {
            state.set(pgnKey, chessGame.pgn)
        } else chessGame.resume(savedPgn)

        chessGame.pov = chessGame.currentPlayer
    }

    /**
     * Responsible for notifying ChessGame of click event and evaluate if a promotion is occurring
     */
    fun selectSquare(row: Int, col: Int): Boolean {
        val moved = chessGame.selectSquare(row, col)
        if (moved)
            changePov()

        boardLiveData.postValue(chessGame.board)

        return moved
    }

    /**
     * Notifies ChessGame of a changing of point of view
     */
    private fun changePov(): Army {
        return chessGame.flipBoard()
    }

    /**
     * Notifies ChessGame of player's promotion selected piece
     */
    fun promote(pieceType: Type) {
        chessGame.promote(pieceType)
        boardLiveData.postValue(chessGame.board)
    }

    /**
     * Check previous move.
     */
    fun moveBack() {
        chessGame.checkPreviousMove()
        boardLiveData.postValue(chessGame.board)
    }

    /**
     * Check next move.
     */
    fun moveForward() {
        chessGame.checkNextMove()
        boardLiveData.postValue(chessGame.board)
    }
}