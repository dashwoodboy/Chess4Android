package pt.isel.pdm.chess4android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.isel.pdm.chess4android.chess.ChessGame
import pt.isel.pdm.chess4android.chess.Queen
import java.util.*

class ChessRulesTest {

    @Test
    fun pawnMovesTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/6pp/7P/8/8/1P6/4K3")

        // white h5 pawn should only be able to take g6 pawn
        assertEquals(
            LinkedList(
                listOf(
                    game.board[22]
                )
            ), game.possibleMovesWhite[game.board[31]]
        )

        // white b2 pawn is in starting position so it should be able to go to b3 or b4
        assertEquals(
            LinkedList(
                listOf(
                    game.board[41],
                    game.board[33]
                )
            ), game.possibleMovesWhite[game.board[49]]
        )
    }

    @Test
    fun knightMovesTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/6p1/8/5N2/8/4K3")

        // white knight should be able to do one of the following:
        // move to one of these squares: d4, e5, h4, h2, g1, d2
        // take g5,
        // and shouldn't be able to move to e1 because a friendly piece is there
        assertEquals(
            LinkedList(
                listOf(
                    game.board[35],
                    game.board[28],
                    game.board[30],
                    game.board[39],
                    game.board[55],
                    game.board[62],
                    game.board[51]
                )
            ), game.possibleMovesWhite[game.board[45]]
        )
    }

    @Test
    fun bishopMovesTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/6p1/8/8/3B4/4K3")

        // white bishop should be able to do one of the following:
        // move to one of these squares: a5, b4, c3, c1, e3, f4
        // take g5,
        // and shouldn't be able to move to e1 because a friendly piece is there
        assertEquals(
            LinkedList(
                listOf(
                    game.board[42],
                    game.board[33],
                    game.board[24],
                    game.board[44],
                    game.board[37],
                    game.board[30],
                    game.board[58],
                )
            ), game.possibleMovesWhite[game.board[51]]
        )
    }

    @Test
    fun rookMovesTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/6p1/8/8/8/4K1R1")

        // white rook should be able to do one of the following:
        // move to one of these squares: f1, h1, g2, g3, g4
        // take g5,
        // and shouldn't be able to move to e1 because a friendly piece is there
        assertEquals(
            LinkedList(
                listOf(
                    game.board[54],
                    game.board[46],
                    game.board[38],
                    game.board[30],
                    game.board[61],
                    game.board[63]
                )
            ), game.possibleMovesWhite[game.board[62]]
        )
    }

    @Test
    fun queenMovesTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/6p1/8/8/3Q4/4K3")

        // white queen should be able to do one of the following:
        // move to one of these squares: c1, d1, a2, b2, c2, e2, f2, g2, h2, a5. b4, c3, d3, d4, d5,
        //      d6, d7, d8, e3, f4
        // take g5,
        // and shouldn't be able to move to e1 because a friendly piece is there
        assertEquals(
            LinkedList(
                listOf(
                    game.board[42],
                    game.board[33],
                    game.board[24],
                    game.board[43],
                    game.board[35],
                    game.board[27],
                    game.board[19],
                    game.board[11],
                    game.board[3],
                    game.board[44],
                    game.board[37],
                    game.board[30],
                    game.board[52],
                    game.board[53],
                    game.board[54],
                    game.board[55],
                    game.board[59],
                    game.board[58],
                    game.board[50],
                    game.board[49],
                    game.board[48]
                )
            ), game.possibleMovesWhite[game.board[51]]
        )
    }

    @Test
    fun kingMovesTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/6p1/q7/8/3Q4/4K2R")

        // white king should be able to do one of the following:
        // move to one of these squares: d1, e2
        // short castle
        // and shouldn't be able to move to d2 because a friendly piece is there
        assertEquals(
            LinkedList(
                listOf(
                    game.board[52],
                    game.board[53],
                    game.board[61],
                    game.board[62]
                )
            ), game.possibleMovesWhite[game.board[60]]
        )
    }

    @Test
    fun simpleMoveTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/6p1/8/8/6P1/4K3")

        // move g2 pawn to g4
        game.selectSquare(6, 6)
        game.selectSquare(4, 6)

        assertEquals("4k3/8/8/6p1/6P1/8/8/4K3", game.saveToFEN())
    }

    @Test
    fun captureTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/6p1/8/5p2/6P1/4K3")

        // g2 pawn captures f3 pawn
        game.selectSquare(6, 6)
        game.selectSquare(5, 5)

        assertEquals("4k3/8/8/6p1/8/5P2/8/4K3", game.saveToFEN())
    }

    @Test
    fun pinnedPieceTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/1b6/2N5/8/4K3")

        // white knight shouldn't be able to move because the enemy bishop pins it to the king
        assertEquals(null, game.possibleMovesWhite[game.board[42]])
    }

    @Test
    fun shortCastleTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/8/8/8/R3K2R")

        // white short castles
        game.selectSquare(7, 4)
        game.selectSquare(7, 6)

        assertEquals("4k3/8/8/8/8/8/8/R4RK1", game.saveToFEN())
    }

    @Test
    fun longCastleTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/8/8/8/R3K2R")

        // white long castles
        game.selectSquare(7, 4)
        game.selectSquare(7, 2)

        assertEquals("4k3/8/8/8/8/8/8/2KR3R", game.saveToFEN())
    }

    @Test
    fun cantCastleDueToEnemyTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/1b6/8/8/8/R3K2R")

        // white king should be able to do one of the following:
        // move to one of these squares: d1, d2, f2
        // shouldn't be able to short castle due to enemy bishop eyeing f1
        assertEquals(
            LinkedList(
                listOf(
                    game.board[51],
                    game.board[53],
                    game.board[59],
                    game.board[58]
                )
            ), game.possibleMovesWhite[game.board[60]]
        )
    }

    @Test
    fun enPassantTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/1p6/8/2P5/4K3")

        // c2 pawn moves to c4
        game.selectSquare(6, 2)
        game.selectSquare(4, 2)

        // b4 takes c4 en passant
        game.selectSquare(4, 1)
        game.selectSquare(5, 2)

        assertEquals("4k3/8/8/8/8/2p5/8/4K3", game.saveToFEN())
    }

    @Test
    fun promotionTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/7P/8/8/8/8/8/4K3")

        // h7 pawn moves to h8
        game.selectSquare(1, 7)
        game.selectSquare(0, 7)

        // complete promotion to queen
        game.promote(Queen::class.java)

        assertEquals("4k2Q/8/8/8/8/8/8/4K3", game.saveToFEN())
    }

    @Test
    fun checkTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/8/1N6/3b4/R3K3")

        game.checkEndgameConditions()

        assertTrue(game.whiteKing.checked)
        assertTrue(game.state == ChessGame.State.Playing)
    }

    @Test
    fun movesWhileCheckedTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/8/1N6/3b4/R3K3")

        // white king should be able to do one of the following:
        // move to one of these squares: d1, e2, f2, f1
        // take d2
        assertEquals(
            LinkedList(
                listOf(
                    game.board[51],
                    game.board[52],
                    game.board[53],
                    game.board[61],
                    game.board[59]
                )
            ), game.possibleMovesWhite[game.board[60]]
        )

        // white knight should be able to:
        // take d2
        assertEquals(
            LinkedList(
                listOf(
                    game.board[51]
                )
            ), game.possibleMovesWhite[game.board[41]]
        )

        // white rook shouldn't be able to do anything
        assertEquals(null, game.possibleMovesWhite[game.board[56]])
    }

    @Test
    fun checkmateTest() {
        val game = ChessGame()

        game.loadFromFEN("8/8/8/8/8/4k3/4q3/4K3")

        game.checkEndgameConditions()

        assertTrue(game.state == ChessGame.State.BlackByCheckmate)
    }

    @Test
    fun stalemateTest() {
        val game = ChessGame()

        game.loadFromFEN("8/8/8/8/8/3q1k2/8/4K3")

        game.checkEndgameConditions()

        assertTrue(game.state == ChessGame.State.Stalemate)
    }
}