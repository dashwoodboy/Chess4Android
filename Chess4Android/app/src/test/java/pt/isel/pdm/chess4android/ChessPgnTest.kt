package pt.isel.pdm.chess4android

import org.junit.Test

import org.junit.Assert.*
import pt.isel.pdm.chess4android.chess.ChessGame
import pt.isel.pdm.chess4android.chess.ChessPuzzle

class ChessPgnTest {

    @Test
    fun fullPuzzleLoadTest() {
        val puzzle = ChessPuzzle()

        val solution = arrayOf(
            "f2f4",
            "e4f3",
            "f1e1",
            "e5d6",
            "e1d1",
            "d6c7",
            "d1d8"
        )
        puzzle.start("d4 d5 c4 c6 Nf3 Bf5 Nc3 Nf6 Bg5 Nbd7 c5 h6 Bh4 e6 e3 Be7 Bd3 Ne4 Bxe7 Qxe7 Bxe4 Bxe4 Nxe4 dxe4 Nd2 f5 Qh5+ Qf7 Qxf7+ Kxf7 g4 Nf6 h3 h5 g5 Nd7 h4 e5 Nc4 exd4 exd4 b6 b4 bxc5 bxc5 Rab8 Kd2 Rhd8 Kc3 Nf8 Rab1 g6 a4 Ne6 Ne5+ Ke8 Rxb8 Rxb8 Nxc6 Rb7 Ne5 Nf4 Kc4 Ke7 d5 a6 Nc6+ Kd7 Na5 Rb2 Rf1 Nd3 Kc3 Ra2 Nc4 Nxc5 Ne5+ Kd6 Nxg6 Kxd5 Nf4+ Ke5 Nxh5 Rxa4 g6 Rd4 g7 Rd8 Kc4 Ne6".split(" ").toMutableList(),
            solution.toMutableList()
        )

        assertEquals("3r4/6P1/p3n3/4kp1N/2K1p2P/8/5P2/5R2", puzzle.saveToFEN())
    }

    @Test
    fun twoPiecesCanMoveTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/5p2/3N4/6N1/4K3")

        game.readMove("Ngxf4")

        assertEquals("4k3/8/8/8/5N2/3N4/8/4K3", game.saveToFEN())
    }

    @Test
    fun enPassantTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/5pP1/8/8/8/4K3")

        game.readMove("gxf6")

        assertEquals("4k3/8/5P2/8/8/8/8/4K3", game.saveToFEN())
    }

    @Test
    fun promotionTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/7P/8/8/8/8/8/4K3")

        game.readMove("h8=Q")

        assertEquals("4k2Q/8/8/8/8/8/8/4K3", game.saveToFEN())
    }

    @Test
    fun shortCastleTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/8/8/8/R3K2R")

        // white short castles
        game.readMove("O-O")

        assertEquals("4k3/8/8/8/8/8/8/R4RK1", game.saveToFEN())
    }

    @Test
    fun longCastleTest() {
        val game = ChessGame()

        game.loadFromFEN("4k3/8/8/8/8/8/8/R3K2R")

        // white long castles
        game.readMove("O-O-O")

        assertEquals("4k3/8/8/8/8/8/8/2KR3R", game.saveToFEN())
    }
}