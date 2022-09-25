package pt.isel.pdm.chess4android.chess

import java.util.*

/**
 * Pawn piece. Moves 1 square forward and takes 1 square in it's front diagonal.
 * On it's first move it can jump 2 squares instead of 1.
 *
 * If a enemy pawn jumps 2 squares to reach the position next to it, it can take that pawn
 * as if it was normally in it's front diagonal (en passant). Only available for 1 move.
 *
 * If it reaches the opposite site of the board, it can promote to another piece (promotion).
 */
class Pawn(index: Int, army: Army): Piece(index, army) {

    override fun calculateMoves(isCurrentPlayer: Boolean, getElement: (Int) -> Element?): List<Int> {
        val list = LinkedList<Int>()

        var targetIdx = addOffset(index)

        var element = getElement(targetIdx)

        if (isCurrentPlayer && element != null && element is Empty) {
            list.add(targetIdx)
            if (hasntMoved()) {
                targetIdx = addOffset(targetIdx)
                element = getElement(targetIdx)
                if (element != null && element is Empty)
                    list.add(targetIdx)
            }
        }

        for (idx in getDiagonalOffset()) {
            if (checkPieceBounds(targetIdx, idx)) {
                element = getElement(idx)
                if (element != null && ((element is Piece && element.army != army) || !isCurrentPlayer))
                    list.add(idx)
            }
        }

        val row = index / 8

        // Check en passant
        if (isCurrentPlayer && (army == Army.WHITE && row == 3) || (army == Army.BLACK && row == 4)) {
            for (idx in listOf(-1 + index, 1 + index)) {
                element = getElement(idx)
                if (element != null && ((element is Pawn && element.army != army))) {
                    val offset = if (element.army == Army.WHITE) 2 * ChessBoard.SIZE else -(2 * ChessBoard.SIZE) // 2 rows are 2*8 in our list
                    val enemyStartingSquare = getElement(idx + offset)

                    if (element.lastMoved && enemyStartingSquare != null && enemyStartingSquare.lastMoved) {
                        list.add(idx + (offset / 2))
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
        val distanceBetweenCols = (index % ChessBoard.SIZE) - (options % ChessBoard.SIZE)

        return distanceBetweenCols > -2 && distanceBetweenCols < 2
    }

    private fun getDiagonalOffset() = listOf(addOffset(index) + 1, addOffset(index) - 1)

    private fun addOffset(index: Int) = if(army == Army.WHITE) index - 8 else  index + 8

    private fun hasntMoved(): Boolean = if (army == Army.WHITE) index / 8 == 6 else index / 8 == 1
}