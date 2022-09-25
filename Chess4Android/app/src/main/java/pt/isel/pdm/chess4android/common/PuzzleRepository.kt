package pt.isel.pdm.chess4android.common

import pt.isel.pdm.chess4android.history.HistoryPuzzleDao
import pt.isel.pdm.chess4android.history.PuzzleEntity
import retrofit2.await
import java.lang.Exception
import java.net.SocketTimeoutException
import java.util.*

/**
 * Extension function of [PuzzleEntity] to conveniently convert it to a [PuzzleOfDay] instance.
 * Only relevant for this activity.
 */
fun PuzzleEntity.toPuzzleOfDay() = PuzzleOfDay(
    game = Game(this.id, this.pgn),
    puzzle = Puzzle(this.puzzle.split(", ").toTypedArray())
)

/**
 * Returns date based on the refresh rate of Lichess Daily Puzzle api.
 * Always returns the date of puzzle referring to the current daily puzzle in GMT+00:00
 */
fun getCurrentDate(): String {
    val tz = TimeZone.getTimeZone("GMT+0")
    val calendar = Calendar.getInstance(tz)

    val day = if (calendar.get(Calendar.HOUR_OF_DAY) >= 13) calendar.get(Calendar.DAY_OF_MONTH)
        else calendar.get(Calendar.DAY_OF_MONTH) - 1

    val month = if (calendar.get(Calendar.MONTH) + 1 <= 10) "0" + (calendar.get(Calendar.MONTH) + 1)
        else "" + (calendar.get(Calendar.MONTH) + 1)

    return String.format("%s-%s-%s", calendar.get(Calendar.YEAR), month, day)
}

/**
 * Repository for the Daily Chess Puzzle
 */
class PuzzleRepository(
    private val puzzleOfDayService: PuzzleOfDayService,
    private val historyPuzzleDao: HistoryPuzzleDao
) {

    /**
     * Gets the daily puzzle from the local DB, if available.
     * Else it requests from the Lichess API
     */
    suspend fun fetchPuzzleOfDay(): PuzzleOfDay? {
        val todayGame = getCurrentDate()
        val lastEntry = historyPuzzleDao.getLast(1).firstOrNull()
        return if (todayGame == lastEntry?.date) lastEntry.toPuzzleOfDay()
            else fetchFromApi()

    }

    /**
     * Gets the daily puzzle from the Lichess API.
     */
    private suspend fun fetchFromApi(): PuzzleOfDay? {
        try {
            val response = puzzleOfDayService.getPuzzle().await()
            insertIntoDB(response)
            return response
        } catch (e: SocketTimeoutException) {
            return null
        }
    }

    /**
     * Saves the daily quote to the local DB.
     */
    private suspend fun insertIntoDB(puzzleOfDay: PuzzleOfDay) {
        try {
            historyPuzzleDao.insert(
                PuzzleEntity(
                    id = puzzleOfDay.game.id,
                    pgn = puzzleOfDay.game.pgn,
                    puzzle = puzzleOfDay.puzzle.solution.joinToString(),
                    completed = 0,
                    date = getCurrentDate()
                )
            )
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Get all the puzzles in the Room database.
     */
    suspend fun getAllHistory(): List<PuzzleDTO>{
        return historyPuzzleDao.getALl().map {
            PuzzleDTO(
                date = it.date,
                puzzle = PuzzleOfDay(
                    Game(it.id ,it.pgn),
                    Puzzle(it.puzzle.split(", ").toTypedArray())
                ),
                completed = it.completed
            )
        }
    }

    suspend fun setCompleted(date: String) {
        historyPuzzleDao.setPuzzleToCompleted(date)
    }
}