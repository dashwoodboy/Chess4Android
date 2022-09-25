package pt.isel.pdm.chess4android.chess

/**
 * Chess Puzzle mode.
 * One player has to crack the puzzle and perform the right set of moves.
 */
class ChessPuzzle : ChessBoard() {

    private lateinit var solution: MutableList<String>

    var wonPuzzle = false
    var moved = false // Signals that the player made a correct move

    /**
     * Starts the puzzle by making the moves in the pgn String.
     */
    fun start(pgn: MutableList<String>, solution: MutableList<String>) {
        this.pgn = pgn
        this.solution = solution

        readPgn(pgn)
    }

    /**
     * Resumes the puzzle in case of app crash.
     */
    fun resume(savedPgn: MutableList<String>, savedSolution: MutableList<String>) {
        pgn = savedPgn
        solution = savedSolution

        readPgn(savedPgn)

        if(savedSolution.isEmpty()) {
            wonPuzzle = true
            cantSelect = true
        }
    }

    /**
     * Processes the player's move
     * If they made the correct move then it will play and if there's
     * more to the solution, it will call to play that move.
     */
    override fun move(from: Element, to: Element) {
        moved = false
        if (checkSolution(from.index, to.index)) {
            moved = true
            switchPositions(from.index, to.index, true)
            if (solution.size > 0) {
                switchPlayer()
                nextMove()
            } else {
                cantSelect = true
                wonPuzzle = true
            }
        }
    }

    /**
     * Checks if the player made the move corresponding to the solution.
     */
    private fun checkSolution(playerFrom: Int, playerTo: Int): Boolean {
        val solutionString = solution[0]
        val solutionFrom = pgnCoordinateToIndex(solutionString.substring(0,2))
        val solutionTo = pgnCoordinateToIndex(solutionString.substring(2,4))

        if(playerFrom == solutionFrom && playerTo == solutionTo){
            solution.removeFirst()
            return true
        }
        return false
    }

    /**
     * Plays the next move in the solution.
     * Return true if the solution is over.
     */
    fun nextMove(): Boolean {
        val moveString = solution.removeFirst()
        switchPositions(pgnCoordinateToIndex(moveString.substring(0,2)),pgnCoordinateToIndex(moveString.substring(2,4)), true)

        if (solution.size > 0) {
            switchPlayer()
            calculatePossibleMoves()
        } else return true

        return false
    }
}