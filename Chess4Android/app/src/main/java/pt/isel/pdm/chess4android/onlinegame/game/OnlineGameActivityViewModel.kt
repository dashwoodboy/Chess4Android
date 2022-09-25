package pt.isel.pdm.chess4android.onlinegame.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.chess4android.chess.Army
import pt.isel.pdm.chess4android.chess.ChessBoard
import pt.isel.pdm.chess4android.chess.ChessGame
import pt.isel.pdm.chess4android.chess.Element
import pt.isel.pdm.chess4android.common.ChessApplication
import java.lang.reflect.Type

/**
 * The actual execution host behind the application's Online Game screen (i.e. the [OnlineGameActivity]).
 */
class OnlineGameActivityViewModel(
    app: Application,
    private val gameState: GameState, private val playerColor: Army
) : AndroidViewModel(app) {
    private var requested = false

    val chessGame = ChessGame()

    /**
     * Subscription to changes on document with the game id on Firestore
     */
    private val gameSubscription = getApplication<ChessApplication>()
        .onlineGameRepository.subscribeToGameStateChanges(
            challengeId = gameState.id,
            onSubscriptionError = { error(it) },
            onGameStateChange = { remoteMove(it) }
        )

    /**
     * The [LiveData] instance used to publish the [ChessBoard].
     */
    val boardLiveData: MutableLiveData<MutableList<Element>> = MutableLiveData(chessGame.board)

    /**
     * The [LiveData] instance used to publish changes on game state
     */
    val stateLiveData: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * The [LiveData] instance used to publish draw request
     */
    val drawRequestLiveData: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * The initial point of view is decided based on player color
     */
    init {
        chessGame.pov = playerColor
        if (playerColor == Army.BLACK)
            chessGame.cantSelect = true
    }

    /**
     * Responsible for notifying ChessGame of click event and evaluate if a promotion is occurring
     */
    fun selectSquare(row: Int, col: Int): Boolean {
        val moved = chessGame.selectSquare(row, col)
        if (moved && !chessGame.isPromoting)
            updateGameState()

        boardLiveData.postValue(chessGame.board)

        return moved
    }

    /**
     * Notifies ChessGame of a changing of point of view
     */
    fun changePov(): Army {
        return chessGame.flipBoard()
    }

    /**
     * Notifies ChessGame of player's promotion selected piece
     */
    fun promote(pieceType: Type) {
        chessGame.promote(pieceType)
        updateGameState()

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

    /**
     * Notifies ChessGame on a intent to resign
     */
    fun resign() {
        if (chessGame.state != ChessGame.State.Playing)
            return
        gameState.resign = playerColor
        updateRemote(gameState)
        chessGame.resign(playerColor)
    }

    /**
     * Notifies ChessGame on a request to draw that needs to be accepted
     */
    fun requestDraw() {
        if (chessGame.state != ChessGame.State.Playing)
            return
        gameState.draw = playerColor
        requested = true
        updateRemote(gameState)

    }

    /**
     * Notifies ChessGame the answer to the request draw
     */
    fun responseDraw(response: Boolean) {
        if (response) {
            chessGame.drawAgreed()
            gameState.draw = playerColor
            stateLiveData.value = true
        } else {
            gameState.draw = null
        }
        updateRemote(gameState)
    }

    /**
     * Update the current instance of GameState with the next player and latest move,
     * and call  updateRemote for update data on Firestore
     */
    private fun updateGameState() {
        gameState.army = if (playerColor == Army.WHITE) Army.BLACK else Army.WHITE
        gameState.lastMove = chessGame.pgn.last()
        gameState.draw = null
        updateRemote(gameState)
        chessGame.cantSelect = true
    }

    /**
     * Call repository's function updateGameState to update the GameState instance on Firestore,
     * with the current local instance of GameState
     */
    private fun updateRemote(gameState: GameState) {
        getApplication<ChessApplication>()
            .onlineGameRepository
            .updateGameState(gameState) { result ->
                result.onFailure { error(it) }
            }
    }

    /**
     * Read GameState received from Firestore.
     * This function is responsible for update local board with the latest move of opponent received
     * from Firestore, and also evaluate the intent to resign or request a draw
     */
    private fun remoteMove(gameState: GameState) {
        if (gameState.resign != null) {
            chessGame.state =
                if (gameState.resign == Army.WHITE) ChessGame.State.WhiteResigns else ChessGame.State.BlackResigns
            stateLiveData.value = true
            return
        }
        if (gameState.draw != null && gameState.draw != playerColor) {
            if (requested) {
                chessGame.state = ChessGame.State.DrawAgreed
                stateLiveData.value = true
                chessGame.drawAgreed()
            } else
                drawRequestLiveData.value = true

            return
        }
        if (gameState.lastMove != "" && gameState.army == playerColor) {
            chessGame.opponentMove(gameState.lastMove)
            if (chessGame.state == ChessGame.State.Playing)
                chessGame.cantSelect = false
            else stateLiveData.postValue(true)
            boardLiveData.postValue(chessGame.board)
        }

    }

    /**
     * Removes the current game from Firestore when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        getApplication<ChessApplication>().onlineGameRepository.deleteGame(
            challengeId = gameState.id,
            onComplete = { }
        )
        gameSubscription.remove()
    }
}