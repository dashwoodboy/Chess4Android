package pt.isel.pdm.chess4android.daily

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.pdm.chess4android.chess.Army
import pt.isel.pdm.chess4android.chess.ChessBoard
import pt.isel.pdm.chess4android.chess.ChessPuzzle
import pt.isel.pdm.chess4android.chess.Element
import pt.isel.pdm.chess4android.common.ChessApplication
import pt.isel.pdm.chess4android.common.PuzzleOfDay

/**
 * The actual execution host behind the application's Daily Puzzle screen (i.e. the [DailyPuzzleActivity]).
 */
class DailyPuzzleActivityViewModel(
    application: Application,
    val state: SavedStateHandle
): AndroidViewModel(application) {

    private val pgnKey = "PGN"
    private val puzzleKey = "PUZZLE"

    val chessPuzzle = ChessPuzzle()

    /**
     * The [LiveData] instance used to publish the [ChessBoard].
     */
    val boardLiveData: MutableLiveData<MutableList<Element>> = MutableLiveData(chessPuzzle.board)

    fun selectSquare (row: Int, col: Int): Boolean {
        val attemptedMove = chessPuzzle.selectSquare(row, col)
        boardLiveData.postValue(chessPuzzle.board)

        return attemptedMove
    }

    /**
     * Starts the [ChessPuzzle]. If it's the first time it sets all variables in state, else it retrieves these.
     */
    fun start(puzzleOfDay: PuzzleOfDay) {
        val savedPgn = state.get<MutableList<String>>(pgnKey)
        if (savedPgn == null) {
            val solution = puzzleOfDay.puzzle.solution.toMutableList()
            state.set(puzzleKey, solution)

            val pgn = puzzleOfDay.game.pgn.split(" ").toMutableList()
            state.set(pgnKey, pgn)
            chessPuzzle.start(pgn, solution)
        } else {
            val savedSolution = state.get<MutableList<String>>(puzzleKey)
            if (savedSolution != null) {
                chessPuzzle.resume(savedPgn, savedSolution)
            }
        }

        chessPuzzle.pov = chessPuzzle.currentPlayer
    }

    /**
     * Change the POV of the board (180º flip).
     */
    fun changePov(): Army {
        return chessPuzzle.flipBoard()
    }

    /**
     * Play the next move in the solution.
     */
    fun nextMove(): Boolean {
        val end = chessPuzzle.nextMove()

        boardLiveData.postValue(chessPuzzle.board)

        return end
    }

    /**
     * Update the puzzle database to set the current puzzle to completed.
     */
    fun setCompleted(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<ChessApplication>().puzzlesRepository.setCompleted(date)
        }
    }

    /**
     * Check previous move.
     */
    fun moveBack() {
        chessPuzzle.checkPreviousMove()
        boardLiveData.postValue(chessPuzzle.board)
    }

    /**
     * Check next move.
     */
    fun moveForward() {
        chessPuzzle.checkNextMove()
        boardLiveData.postValue(chessPuzzle.board)
    }
}