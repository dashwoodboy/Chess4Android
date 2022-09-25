package pt.isel.pdm.chess4android.chess

import java.util.*

/**
 * Bishop piece. Moves only in diagonals.
 */
class Bishop(index: Int, army: Army): Piece(index, army) {

    companion object {
        val offsets: List<Int> = listOf(
            -9, // NW
            -7, // NE
            7, // SW
            9 // SE
        )
    }

    override fun calculateMoves(isCurrentPlayer: Boolean, getElement: (Int) -> Element?): List<Int> {
        val list = LinkedList<Int>()

        for (offset in offsets) {
            var targetIdx = index + offset

            while(true) {
                if (checkPieceBounds(targetIdx, 0)) {
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
                } else break
                targetIdx += offset
            }
        }
        return list
    }

    /**
     * To avoid moving to illegal squares and because the chess board is
     * represented with a list and not a 2D array, we need to check if
     * target squares belong to the same diagonals as the Bishop.
     */
    override fun checkPieceBounds(targetIdx: Int, options: Int): Boolean {
        val differenceBetweenCols = index % ChessBoard.SIZE - targetIdx % ChessBoard.SIZE
        val differenceBetweenRows = index / ChessBoard.SIZE - targetIdx / ChessBoard.SIZE
        val differenceBetweenDifference = differenceBetweenCols + differenceBetweenRows

        return differenceBetweenCols == differenceBetweenRows
                || differenceBetweenDifference == 0
    }
}