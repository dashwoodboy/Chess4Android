package pt.isel.pdm.chess4android.chess

import java.util.*

/**
 * Queen piece. Moves through rows, columns and diagonals. (Mix of Rook and Bishop)
 */
class Queen(index: Int, army: Army): Piece(index, army) {

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

    override fun calculateMoves(isCurrentPlayer: Boolean, getElement: (Int) -> Element?): List<Int> {
        val list = LinkedList<Int>()
        for ((offsetIdx, offset) in offsets.withIndex()) {
            var targetIdx = index + offset

            while(true) {
                if (checkPieceBounds(targetIdx, offsetIdx)) {
                    val element = getElement(targetIdx)
                    if (element != null)
                        when {
                            element is Empty -> {
                                list.add(targetIdx)
                            }
                            (element as Piece).army != army -> {
                                list.add(targetIdx)
                                break
                            }
                            element.army == army && !isCurrentPlayer -> {
                                list.add(targetIdx)
                                break
                            }
                            else -> break
                        }
                    else break
                } else break
                targetIdx += offset
            }
        }
        return list
    }

    /**
     * To avoid moving to illegal squares and because the chess board is
     * represented with a list and not a 2D array, we need to check if
     * target squares belong to either the same row or column or
     * one of the diagonals.
     */
    override fun checkPieceBounds(targetIdx: Int, options: Int): Boolean {
        // diagonals
        val differenceBetweenCols = index % ChessBoard.SIZE - targetIdx % ChessBoard.SIZE
        val differenceBetweenRows = index / ChessBoard.SIZE - targetIdx / ChessBoard.SIZE
        val differenceBetweenDifference = differenceBetweenCols + differenceBetweenRows
        // row and column
        val rowIsDifferent = targetIdx / ChessBoard.SIZE != index / ChessBoard.SIZE
        val colIsDifferent = targetIdx % ChessBoard.SIZE != index % ChessBoard.SIZE

        return if (options % 2 == 0)
            (differenceBetweenCols == differenceBetweenRows
                    || differenceBetweenDifference == 0)
            else(!rowIsDifferent || !colIsDifferent)
    }
}