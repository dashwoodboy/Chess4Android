package pt.isel.pdm.chess4android.chess

import java.util.*

/**
 * Rook piece. Moves only in rows and columns.
 * Also has the castle ability.
 */
class Rook(index: Int, army: Army): Piece(index, army) {

    companion object {
        val offsets: List<Int> = listOf(
            -8, // UP
            8, // DOWN
            -1, // LEFT
            1 // RIGHT
        )
    }

    override fun calculateMoves(isCurrentPlayer: Boolean, getElement: (Int) -> Element?): List<Int> {
        val list = LinkedList<Int>()

        for (offset in offsets) {
            var targetIdx = index + offset
            var element = getElement(targetIdx)

            while (element != null ) {
                if (!checkPieceBounds(targetIdx, 0)) {
                    break
                }
                if (element is Empty) {
                    list.add(targetIdx)
                    targetIdx += offset
                    element = getElement(targetIdx)
                } else if ((element as Piece).army != army){
                    list.add(targetIdx)
                    break
                } else if (element.army == army && !isCurrentPlayer) {
                    list.add(targetIdx)
                    break
                } else {
                    break
                }
            }
        }
        return list
    }

    /**
     * To avoid moving to illegal squares and because the chess board is
     * represented with a list and not a 2D array, we need to check if
     * target squares belong to either the same row or column.
     */
    override fun checkPieceBounds(targetIdx: Int, options: Int): Boolean {
        return targetIdx / ChessBoard.SIZE == index / ChessBoard.SIZE
                || targetIdx % ChessBoard.SIZE == index % ChessBoard.SIZE
    }
}