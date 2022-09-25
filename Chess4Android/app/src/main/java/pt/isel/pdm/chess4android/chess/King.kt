package pt.isel.pdm.chess4android.chess

import java.util.*

/**
 * King piece. Moves only one square in all directions.
 * Cannot move into a square in which it would be attacked.
 * Also has the castle ability.
 */
class King(index: Int, army: Army): Piece(index, army) {

    companion object {
        val offsets: List<Int> = listOf(
            -9, // NW
            -8, // N
            -7, // NE
            1, // E
            9, // SE
            8, // S
            7, // SW
            -1, // W
        )
    }

    var checked = false

    // Both of these booleans are turned to false if the King moves.
    var canShortCastle = true
    var canLongCastle = true

    override fun calculateMoves(isCurrentPlayer: Boolean, getElement: (Int) -> Element?): List<Int> {
        val list = LinkedList<Int>()

        for (offset in offsets) {
            val targetIdx = index + offset

            if (checkPieceBounds(targetIdx, 0)) {
                val element = getElement(targetIdx)
                if (element != null)
                    if (element is Empty || (element as Piece).army != army
                        || (element.army == army && !isCurrentPlayer))
                        list.add(targetIdx)
            }
        }

        // check castle availability
        if (isCurrentPlayer && !checked) {
            if (canShortCastle) {
                val hSquare = index + 3

                val hElement = getElement(hSquare)
                if (hElement != null) {
                    if (hElement !is Rook) // check if rook moved in order to disable short castle
                        canShortCastle = false
                    else {
                        // f and g square need to be safe and empty
                        val fSquare = index + 1
                        val gSquare = index + 2
                        val fElement = getElement(fSquare)
                        val gElement = getElement(gSquare)
                        if (fElement != null && fElement is Empty && gElement != null && gElement is Empty)
                            list.add(gSquare)
                    }
                }
            }

            if (canLongCastle) {
                val aSquare = index + 3

                val aElement = getElement(aSquare)
                if (aElement != null) {
                    if (aElement !is Rook) // check if rook moved in order to disable short castle
                        canLongCastle = false
                    else {
                        // b square needs to be empty and c and d square need to be safe and empty
                        val bSquare = index - 3
                        val cSquare = index - 2
                        val dSquare = index - 1
                        val bElement = getElement(bSquare)
                        val cElement = getElement(cSquare)
                        val dElement = getElement(dSquare)
                        if (bElement is Empty && cElement != null && cElement is Empty && dElement != null && dElement is Empty)
                            list.add(cSquare)
                    }
                }
            }
        }

        return list
    }

    /**
     * To avoid moving to illegal squares and because the chess board is
     * represented with a list and not a 2D array, we need to check if
     * target squares don't jump from one side of the board to the other.
     */
    override fun checkPieceBounds(targetIdx: Int, options: Int): Boolean {
        val distanceBetweenCols = (index % ChessBoard.SIZE) - (targetIdx % ChessBoard.SIZE)

        return distanceBetweenCols > -2 && distanceBetweenCols < 2
    }
}