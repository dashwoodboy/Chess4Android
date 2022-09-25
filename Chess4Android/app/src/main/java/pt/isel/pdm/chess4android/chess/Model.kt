package pt.isel.pdm.chess4android.chess

/**
 * Represents the player colours.
 */
enum class Army {
    WHITE, BLACK
}

/**
 * Represents an element of a chess board.
 * Contains the its location in the board with it's index,
 * as well as if it's currently targeted by a selected piece.
 */
sealed class Element(open var index: Int) {
    var isTarget = false
    var lastMoved = false
}

/**
 * An empty square
 */
class Empty(index: Int) : Element(index)

/**
 * A piece of the chess game. Has a index and army for location and colour.
 * Also a boolean to see if it's selected.
 * Each type of piece has it's own rules, therefore it's own way of calculating moves.
 */
sealed class Piece(index: Int, open val army: Army): Element(index) {

    var selected: Boolean = false

    abstract fun calculateMoves(isCurrentPlayer: Boolean, getElement: (Int) -> Element?): List<Int>

    abstract fun checkPieceBounds(targetIdx: Int, options: Int): Boolean
}
