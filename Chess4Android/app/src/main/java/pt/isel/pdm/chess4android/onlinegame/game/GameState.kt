package pt.isel.pdm.chess4android.onlinegame.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pt.isel.pdm.chess4android.chess.Army

/**
 * The current GameState info
 *
 * @property [id]       Game unique identifier
 * @property [army]     Current player
 * @property [lastMove] Last move
 * @property [resign]   Resign decision
 * @property [draw]     Request to draw and response
 */
@Parcelize
data class GameState(
    val id: String,
    var army: Army,
    var lastMove: String,
    var resign: Army? = null,
    var draw: Army? = null
): Parcelable
