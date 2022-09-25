package pt.isel.pdm.chess4android.chess

import java.util.*

/**
 * Knight piece. Moves in a L shape pattern, with 2
 * squares in one way and another to complete the L's leg.
 */
class Knight(index: Int, army: Army): Piece(index, army) {

    companion object {
        // Pairs of rows and columns
        val offsets: List<Int> = listOf(-10, -17, -15, -6, 10, 17, 15, 6)
    }

    override fun calculateMoves(isCurrentPlayer: Boolean, getElement: (Int) -> Element?): List<Int> {
        val list = LinkedList<Int>()

        for (offset in offsets) {
            val targetIdx = index + offset

            if (checkPieceBounds(targetIdx, 0)) {
                val element = getElement(targetIdx)
                if (element != null)
                    if (element is Empty || (element as Piece).army != army)
                        list.add(targetIdx)
                    else if (element.army == army && !isCurrentPlayer) {
                        list.add(targetIdx)
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

        return distanceBetweenCols > -3 && distanceBetweenCols < 3
    }
}