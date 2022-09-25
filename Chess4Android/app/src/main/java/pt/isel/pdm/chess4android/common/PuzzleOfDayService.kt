package pt.isel.pdm.chess4android.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import retrofit2.Call
import retrofit2.http.GET

/**
 * 3 classes Representing part of the data returned by the Lichess API.
 * It's existence is due to the API design.
 */
@Parcelize
data class Game(val id: String, val pgn: String) : Parcelable

@Parcelize
data class Puzzle(val solution: Array<String>) : Parcelable

@Parcelize
data class PuzzleOfDay(val game: Game, val puzzle: Puzzle) : Parcelable

/**
 * Represents data returned by the Lichess API and data attributed by us, such as date and completion status.
 * It's [Parcelable] so that it can also be used
 * locally (in the device) to exchange data between activities and as a means to preserve state.
 */
@Parcelize
data class PuzzleDTO(val puzzle: PuzzleOfDay, val date: String, val completed: Int) : Parcelable

/**
 * The abstraction that represents accesses to the Lichess API's resources.
 */
interface PuzzleOfDayService {
    @GET("/api/puzzle/daily")
    fun getPuzzle(): Call<PuzzleOfDay>
}