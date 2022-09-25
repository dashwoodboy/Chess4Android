package pt.isel.pdm.chess4android.chess

import java.lang.reflect.Type

/**
 * Class for PvP Chess Game.
 */
class ChessGame : ChessBoard() {

    enum class State {
        Playing, WhiteByCheckmate, BlackByCheckmate, Draw, Stalemate, DrawAgreed, WhiteResigns, BlackResigns
    }

    var state = State.Playing

    init {
        calculatePossibleMoves()
    }

    /**
     * Resume a game in case of app crash.
     */
    fun resume(savedPgn: MutableList<String>) {
        pgn = savedPgn

        readPgn(savedPgn)
        checkEndgameConditions()
    }

    /**
     * Perform a player move.
     */
    override fun move(from: Element, to: Element) {
        switchPositions(from.index, to.index, true)

        // If in promotion, requires player to choose piece
        if (!isPromoting) {
            switchPlayer()
            calculatePossibleMoves()
            checkEndgameConditions()
        }
    }

    /**
     * Check if game is over.
     */
    fun checkEndgameConditions() {
        // To evaluate end of game conditions, we have to keep in mind
        // that the players have been switched. So if white is now currentPlayer
        // and he has no moves and is checkmated, black wins.
        // We have to look at this way because we only evaluate all possible attacks
        // and mark the king as checked when we calculate the enemy player's moves.
        val king = if (currentPlayer == Army.WHITE) whiteKing else blackKing
        val moves = if (currentPlayer == Army.WHITE) possibleMovesWhite else possibleMovesBlack
        if (king.checked) {
            if (moves.isEmpty()) {
                cantSelect = true
                state = if (king.army == Army.WHITE)
                    State.BlackByCheckmate
                else State.WhiteByCheckmate
            }
        } else if (moves.isEmpty()) {
            cantSelect = true
            state = State.Stalemate
        } else {
            // Need to check if there is still a chance for victory in either side.
            // Basically if either side can't checkmate in any way even with infinite moves
            // then you have insufficient pieces and it's a draw.
            // Since we don't have a chess computer algorithm to analyze the game score, we
            // evaluate the amount of pieces.
            // There are more ways of there being insufficient pieces but the only one
            // we check is if only kings exist on the board.
            var sufficient = false

            for (element in board) {
                if (element is Piece && element !is King)
                    sufficient = true
            }

            if (!sufficient) {
                cantSelect = true
                state = State.Draw
            }
        }
    }

    /**
     * Plays the opponent's move.
     * Used for online play.
     * [move] is in PGN format.
     */
    fun opponentMove(move: String) {
        readMove(move)
        pgn.add(move)
        switchPlayer()
        calculatePossibleMoves()
        checkEndgameConditions()
    }

    /**
     * Promote a pawn based on the [pieceType].
     */
    fun promote(pieceType: Type) {
        if (!isPromoting)
            return

        val row = if (currentPlayer == Army.WHITE) 0 else 7 * SIZE

        for (i in 0 until SIZE) {
            if (board[row + i] is Pawn) {
                lastMoved.remove(board[row + i])
                board[row + i] = (pieceType as Class<*>)
                    .getConstructor(Int::class.java, Army::class.java)
                    .newInstance(row + i, currentPlayer) as Element
                board[row + i].lastMoved = true
                lastMoved.add(board[row + i])

                var pgnMove = pgn.removeLast()
                pgnMove += "=" + when (pieceType) {
                    Queen::class.java -> "Q"
                    Rook::class.java -> "R"
                    Bishop::class.java -> "B"
                    else -> "N"
                }
                pgn.add(pgnMove)
            }
        }

        isPromoting = false

        switchPlayer()
        calculatePossibleMoves()
        checkEndgameConditions()

        cantSelect = false
    }

    /**
     * Signal that players agreed to a draw.
     */
    fun drawAgreed() {
        cantSelect = true
        state = State.DrawAgreed
    }

    /**
     * The given [player] resigned.
     */
    fun resign(player: Army) {
        cantSelect = true
        state = if (player == Army.WHITE) State.WhiteResigns else State.BlackResigns
    }

    /**
     * The current player resigned.
     */
    fun resign() {
        resign(currentPlayer)
    }
}