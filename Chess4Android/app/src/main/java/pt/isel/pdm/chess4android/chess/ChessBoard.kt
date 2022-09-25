package pt.isel.pdm.chess4android.chess

import java.lang.reflect.Type
import java.util.*
import kotlin.collections.HashMap

/**
 * Model for a chess board and parent of type of chess
 * game mode class. Contains the Logic common to all
 * game modes.
 */
sealed class ChessBoard {

    companion object {
        const val SIZE = 8
        const val MAX_SIZE = 64

        const val A_COL = 0
        const val B_COL = 1
        const val C_COL = 2
        const val D_COL = 3
        const val E_COL = 4
        const val F_COL = 5
        const val G_COL = 6
        const val H_COL = 7

        const val a_VALUE = 97 // value of character 'a'
    }

    // Row of pieces in starting position
    private val rowOfPieces = listOf<Type>(
        Rook::class.java,
        Knight::class.java,
        Bishop::class.java,
        Queen::class.java,
        King::class.java,
        Bishop::class.java,
        Knight::class.java,
        Rook::class.java
    )

    var board: MutableList<Element>

    var currentPlayer = Army.WHITE
    var pov = Army.WHITE

    var whiteKing: King
    var blackKing: King

    var possibleMovesWhite = HashMap<Piece, LinkedList<Element>>()
    var possibleMovesBlack = HashMap<Piece, LinkedList<Element>>()

    var isPromoting = false

    var cantSelect = false
    private var selectedPiece: Piece? = null
    protected var lastMoved: LinkedList<Element> = LinkedList()

    var pgn: MutableList<String> = listOf<String>().toMutableList()
    var reviewing = false // if viewing previous moves
    var currentMove = 0 // the current move being shown
    private val pastMoves: LinkedList<Pair<Element, Element>> = LinkedList() // used to navigate back in moves

    abstract fun move(from: Element, to: Element)

    init {
        board = createInitialBoard()
        whiteKing = board[60] as King
        blackKing = board[4] as King
    }

    /**
     * Creates a new board with the pieces in their standard starting locations.
     */
    private fun createInitialBoard(): MutableList<Element> = MutableList(64) { index ->
        val row = index / 8
        val col = index % 8
        val army = if (row >= 6) Army.WHITE else Army.BLACK

        if (row == 0 || row == 7)
            return@MutableList (rowOfPieces[col] as Class<*>)
                .getConstructor(Int::class.java, Army::class.java).newInstance(index, army) as Element
        if (row == 1 || row == 6)
            return@MutableList Pawn(index, army)
        return@MutableList Empty(index)
    }

    /**
     * Selects squares and allows interaction with the board.
     */
    fun selectSquare (row: Int, col: Int): Boolean {
        if (cantSelect || reviewing)
            return false

        val target = board[getIndex(row, col)]

        if (target is Piece && target.army == currentPlayer) {
            if (selectedPiece == null) { //Select one piece
                selectPiece(target)
            } else { // Select another piece or unselect current piece
                val exSelectedPiece: Piece = selectedPiece!!
                unselectPiece()
                if (exSelectedPiece != target)
                    selectPiece(target)
            }
        } else if (target.isTarget) { // take piece
            val exSelectedPiece: Piece = selectedPiece!!
            unselectPiece()

            move(exSelectedPiece, target) // call to abstract implementation

            return true
        } else if (selectedPiece != null) { // unselect piece
            unselectPiece()
        }

        return false
    }

    /**
     * Selects a piece and marks it's square targets.
     */
    private fun selectPiece(piece: Piece) {
        selectedPiece = piece
        piece.selected = true

        val possibleMoves =
            if (currentPlayer == Army.WHITE)
                possibleMovesWhite[piece]
            else possibleMovesBlack[piece]

        if (possibleMoves != null) {
            for (element in possibleMoves) {
                element.isTarget = true
            }
        }
    }

    /**
     * Unselects a piece and removes the marks on it's square targets
     */
    private fun unselectPiece() {
        selectedPiece!!.selected = false
        selectedPiece = null

        for (element in board) {
            if (element.isTarget)
                element.isTarget = false
        }
    }

    /**
     * Removes the previous last moved squares from being highlighted
     */
    private fun removeLastMoved() {
        for (element in lastMoved)
            element.lastMoved = false

        lastMoved.clear()
    }

    /**
     * Adds new squares to be highlighted as last move.
     */
    private fun addLastMoved(sourceIdx: Int, targetIdx: Int) {
        removeLastMoved()

        board[sourceIdx].lastMoved = true
        board[targetIdx].lastMoved = true

        lastMoved.add(board[sourceIdx])
        lastMoved.add(board[targetIdx])
    }

    /**
     * Flips the POV to the other player's.
     */
    fun flipBoard(): Army {
        pov = if (pov == Army.WHITE) Army.BLACK else Army.WHITE
        return pov
    }

    /**
     * Calculates all the possible moves for both players on the games board.
     * For the current player it checks current moves and for the other
     * it checks current moves and which of its pieces are protected.
     *
     * It's required to calculated both players to check pinned pieces
     * and illegal moves for current player's king
     */
    protected fun calculatePossibleMoves() {
        possibleMovesWhite.clear()
        possibleMovesBlack.clear()

        whiteKing.checked = false
        blackKing.checked = false

        val friendlyPieces = LinkedList<Piece>()
        for (element in board) {
            if (element is Piece) {
                // First calculate opponent's moves
                if (element.army == currentPlayer)
                    friendlyPieces.add(element)
                else
                    calculatePossibleMovesForPiece(element, false)
            }
        }

        val currentKing = if (currentPlayer == Army.WHITE) whiteKing else blackKing

        // Now calculate player's moves
        for (piece in friendlyPieces) {
            // Check if piece can move without endangering the king
            if (!currentKing.checked)
                if (piece !is King && !canPieceMove(piece.index))
                    continue
            calculatePossibleMovesForPiece(piece, true)
        }
    }

    /**
     * Calculate possible moves for a piece.
     */
    private fun calculatePossibleMovesForPiece(piece: Piece, isFriendlyPiece: Boolean) {
        val moves = piece.calculateMoves(piece.army == currentPlayer) { index ->
            if (isOffBounds(index) || (piece is King && isDangerSquare(index, piece)))
                return@calculateMoves null
            else return@calculateMoves board[index]
        }

        val map =
            if (piece.army == Army.WHITE) possibleMovesWhite else possibleMovesBlack
        if (moves.isNotEmpty()) {
            map[piece] = LinkedList()
            for (idx in moves) {
                val target = board[idx]
                if (isFriendlyPiece) {
                    if (calculateIfMoveIsLegal(piece.index, target.index))
                        map[piece]?.add(target)
                } else {
                    // See if king is in check
                    if (target is King && target.army != piece.army) {
                        if (target.army == Army.WHITE)
                            whiteKing.checked = true
                        else blackKing.checked = true
                    }
                    map[piece]?.add(target)
                }
            }
        }
        if (map[piece]?.isEmpty() == true)
            map.remove(piece)
    }

    /**
     * Checks if a piece can move without endangering it's king.
     */
    private fun canPieceMove(idx: Int): Boolean {
        val analyzeBoard = cloneBoard()

        analyzeBoard[idx] = Empty(idx)

        return calculateIfBoardIsSafe(analyzeBoard)
    }

    /**
     * Check if a move is legal and saves king from check.
     */
    private fun calculateIfMoveIsLegal(sourceIdx: Int, targetIdx: Int): Boolean {
        val analyzeBoard = cloneBoard()

        val tmp = analyzeBoard[targetIdx]

        if (tmp is Empty) {
            analyzeBoard[targetIdx] = analyzeBoard[sourceIdx]
            analyzeBoard[sourceIdx] = tmp

            analyzeBoard[sourceIdx].index = targetIdx
            analyzeBoard[targetIdx].index = sourceIdx
        } else {
            analyzeBoard[targetIdx] = analyzeBoard[sourceIdx]
            analyzeBoard[targetIdx].index = sourceIdx

            analyzeBoard[sourceIdx] = Empty(sourceIdx)
        }

        return calculateIfBoardIsSafe(analyzeBoard)
    }

    /**
     * Calculates the move on a imaginary board to see if the current player's king is in check.
     * Used to check if moves a legal or not.
     */
    private fun calculateIfBoardIsSafe(analyzeBoard: MutableList<Element>): Boolean {
        for (element in analyzeBoard) {
            if (element is Piece) {
                if (element.army != currentPlayer) {
                    val moves = element.calculateMoves(element.army == currentPlayer) { index ->
                        if (isOffBounds(index) || (element is King && isDangerSquare(index, element)))
                            return@calculateMoves null
                        else return@calculateMoves analyzeBoard[index]
                    }

                    for (idx in moves) {
                        val target = analyzeBoard[idx]
                        if (target is King && target.army != element.army)
                            return false
                    }
                }
            }
        }

        return true
    }

    /**
     * Clones the current board with fully new elements.
     */
    private fun cloneBoard(): MutableList<Element> {
        return MutableList(64) {
            return@MutableList cloneElement(it)
        }
    }

    /**
     * Clones a specific square.
     */
    private fun cloneElement(index: Int): Element {
        return when(val origin = board[index]) {
            is King -> King(origin.index, origin.army)
            is Queen -> Queen(origin.index, origin.army)
            is Rook -> Rook(origin.index, origin.army)
            is Bishop -> Bishop(origin.index, origin.army)
            is Knight -> Knight(origin.index, origin.army)
            is Pawn -> Pawn(origin.index, origin.army)
            else -> Empty(origin.index)
        }
    }

    /**
     * Clones a specific square.
     */
    private fun cloneElement(element: Element): Element {
        return when(element) {
            is King -> King(element.index, element.army)
            is Queen -> Queen(element.index, element.army)
            is Rook -> Rook(element.index, element.army)
            is Bishop -> Bishop(element.index, element.army)
            is Knight -> Knight(element.index, element.army)
            is Pawn -> Pawn(element.index, element.army)
            else -> Empty(element.index)
        }
    }

    /**
     * Evaluates if that square is dangerous for the king to move to.
     */
    private fun isDangerSquare(index: Int, king: King): Boolean {
        if (king.army != currentPlayer)
            return false
        val map = if (king.army == Army.WHITE) possibleMovesBlack
        else possibleMovesWhite
        for (opponentsPossibleMoves in map.values)
            for (dangerSquare in opponentsPossibleMoves)
                if (index == dangerSquare.index)
                    return true
        return false
    }

    /**
     * Switches 2 positions in the board.
     * If the target had a Piece then a Empty object
     * is placed source.
     */
    fun switchPositions(sourceIdx: Int, targetIdx: Int, toRecord: Boolean) {
        addMove(sourceIdx, targetIdx)

        if (toRecord)
            addPgn(sourceIdx, targetIdx)

        val tmp = board[sourceIdx]

        // If promoting
        if (board[sourceIdx] is Pawn && toRecord) {
            if (((board[sourceIdx] as Pawn).army == Army.WHITE && targetIdx / 8 == 0) || ((board[sourceIdx] as Pawn).army == Army.BLACK && targetIdx / 8 == 7)) {
                cantSelect = true
                isPromoting = true
            }
        }

        // If king moves, disable castling
        if (board[sourceIdx] is King) {
            val king = board[sourceIdx] as King
            king.canShortCastle = false
            king.canLongCastle = false
        }

        if (board[targetIdx] is Empty) {
            val sourceCol = sourceIdx % 8
            val targetCol = targetIdx % 8
            if (board[sourceIdx] is Pawn && sourceCol != targetCol) { // If pawn takes en passant
                val offset = if ((board[sourceIdx] as Pawn).army == Army.WHITE) 8 else -8

                board[targetIdx + offset] = Empty(targetIdx + offset)
            } else if (board[sourceIdx] is King ) {
                val differenceBetweenCols = sourceCol - targetCol
                val row = board[sourceIdx].index / 8
                if (differenceBetweenCols > 1) { // If king is long castling
                    val aSquare = row * SIZE + A_COL
                    val dSquare = row * SIZE + D_COL
                    val tmpRook = board[aSquare]
                    board[aSquare] = Empty(aSquare)
                    board[dSquare] = tmpRook
                    tmpRook.index = dSquare
                } else if (differenceBetweenCols < -1) { // If king is short castling
                    val fSquare = row * SIZE + F_COL
                    val hSquare = row * SIZE + H_COL
                    val tmpRook = board[hSquare]
                    board[hSquare] = Empty(hSquare)
                    board[fSquare] = tmpRook
                    tmpRook.index = fSquare
                }
            }

            board[sourceIdx] = board[targetIdx]
            board[sourceIdx].index = sourceIdx
        } else {
            board[sourceIdx] = Empty(sourceIdx)
            val map = if ((board[targetIdx] as Piece).army == Army.WHITE) possibleMovesWhite
            else possibleMovesBlack
            map.remove(board[targetIdx])
        }

        board[targetIdx] = tmp
        board[targetIdx].index = targetIdx

        addLastMoved(sourceIdx, targetIdx)
    }

    /**
     * Add this move to our pgn line.
     */
    private fun addPgn(sourceIdx: Int, targetIdx: Int) {
        val source = board[sourceIdx] as Piece
        val target = board[targetIdx]

        // Need to check castle
        if (board[sourceIdx] is King) {
            val differenceBetweenCols = (sourceIdx % 8) - (targetIdx % 8)
            if (differenceBetweenCols > 1) { // If king is long castling
                pgn.add("O-O-O")
                return
            } else if (differenceBetweenCols < -1) { // If king is short castling
                pgn.add("O-O")
                return
            }
        }

        var newPgnMove = when (source) {
            is King -> "K"
            is Queen -> "Q"
            is Rook -> "R"
            is Bishop -> "B"
            is Knight -> "N"
            else -> ""
        }

        // If en passant
        val sourceCol = sourceIdx % 8
        val targetCol = targetIdx % 8
        if (source is Pawn && sourceCol != targetCol) {
            newPgnMove += indexToPgnCoordinate(sourceIdx)[0] + "x"
        }

        // Need to check if 2 equal pieces aim the same square
        val map = if (source.army == Army.WHITE) possibleMovesWhite else possibleMovesBlack
        for ((piece, moves) in map) {
            if (source.javaClass == piece.javaClass && source.index != piece.index) {
                if (moves.contains(target)) {
                    newPgnMove += if (sourceIdx % 8 != piece.index % 8)
                        ((sourceIdx % 8) + a_VALUE).toChar()
                    else ((SIZE + sourceIdx) * SIZE).toChar()
                }
            }
        }

        // Is the piece taking another
        if (target is Piece) {
            if (source is Pawn)
                newPgnMove += ((sourceIdx % 8) + a_VALUE).toChar()
            newPgnMove += "x"

        }

        newPgnMove += indexToPgnCoordinate(targetIdx)

        pgn.add(newPgnMove)
    }

    /**
     * Adds a move to the pastMoves list for review feature.
     */
    private fun addMove(sourceIdx: Int, targetIdx: Int) {
        if (reviewing)
            return

        val source = cloneElement(sourceIdx)
        val target = cloneElement(targetIdx)

        pastMoves.add(Pair(source, target))
        currentMove++
    }

    /**
     * Changes the board to correspond with the previous move.
     */
    fun checkPreviousMove() {
        if (currentMove <= 0)
            return

        reviewing = true
        currentMove--

        val source = cloneElement(pastMoves[currentMove].first)
        val target = cloneElement(pastMoves[currentMove].second)

        if (target is Empty) {
            val sourceCol = source.index % 8
            val targetCol = target.index % 8

            if (source is Pawn && sourceCol != targetCol) { // If en passant
                val offset = if (source.army == Army.WHITE) 8 else -8
                val opponent = if (source.army == Army.WHITE) Army.BLACK else Army.WHITE
                board[target.index + offset] = Pawn(target.index + offset, opponent)
            } else if (source is King) { // If castle
                val differenceBetweenCols = sourceCol - targetCol
                val row = source.index / 8
                if (differenceBetweenCols > 1) { // If king long castled
                    val aSquare = row * SIZE + A_COL
                    val bSquare = row * SIZE + B_COL
                    val dSquare = row * SIZE + D_COL
                    board[aSquare] = Rook(aSquare, source.army)
                    board[bSquare] = Empty(bSquare)
                    board[dSquare] = Empty(dSquare)
                } else if (differenceBetweenCols < -1) { // If king short castled
                    val fSquare = row * SIZE + F_COL
                    val hSquare = row * SIZE + H_COL
                    board[fSquare] = Empty(fSquare)
                    board[hSquare] = Rook(hSquare, source.army)
                }
            }
        }
        board[source.index] = source
        board[target.index] = target

        if (currentMove == 0)
            removeLastMoved()
        else {
            addLastMoved(pastMoves[currentMove - 1].first.index, pastMoves[currentMove - 1].second.index)
        }

        switchPlayer()
        calculatePossibleMoves()
    }

    /**
     * Checks if there more moves ahead.
     */
    fun canCheckPreviousMove() = currentMove > 0

    /**
     * Changes the board to correspond with the next move.
     */
    fun checkNextMove() {
        if (!reviewing)
            return

        readMove(pgn[currentMove++])

        if (currentMove == pgn.size)
            reviewing = false

        switchPlayer()
        calculatePossibleMoves()
    }

    /**
     * Checks if there more moves behind.
     */
    fun canCheckForwardMove() = currentMove != pgn.size

    /**
     * Switch current player.
     */
    protected fun switchPlayer() {
        currentPlayer = if (currentPlayer == Army.WHITE) Army.BLACK else Army.WHITE
    }

    /**
     * Get list index from row and col.
     */
    private fun getIndex(row: Int, col: Int) = (row * SIZE + col)

    /**
     * Translates from pgn coordinate to index. ie: h8 -> 7
     */
    fun pgnCoordinateToIndex(square: String): Int = (square[0].code - a_VALUE) +
            (SIZE - square[1].digitToInt()) * SIZE

    /**
     * Translates an index to the corresponding board coordinate. ie: 7 -> h8
     */
    private fun indexToPgnCoordinate(index: Int): String = "" + ((index % 8) + a_VALUE).toChar() +
            (SIZE - (index / 8))

    /**
     * Indicates if it's off the board list limits.
     */
    private fun isOffBounds(index: Int): Boolean = index < 0 || index >= MAX_SIZE

    /**
     * Reads the entire pgn line.
     */
    protected fun readPgn(pgn: MutableList<String>) {
        for (move in pgn) {
            readMove(move)
            switchPlayer()
        }

        calculatePossibleMoves()
    }

    /**
     * Read a move in pgn notation and plays it on the board.
     * Assumes the move has the correct format.
     */
    fun readMove(text: String) {
        var move = text
        if (text.contains('+'))
            move = text.replace("+", "")
        else if (text.contains('#'))
            move = text.replace("#", "")

        // If castle
        if (move.contains('-')) {
            val row = if (currentPlayer == Army.BLACK) 0 else 56 // Start idx of row 1 or 8
            if (move == "O-O") { // Short castle (king side)
                switchPositions(row + E_COL, row + G_COL, false)
            } else if (move == "O-O-O") { // Long castle (queen side)
                switchPositions(row + E_COL, row + C_COL, false)
            }
            return
        }

        // Gets the square the piece is moving to/taking at
        val targetIdx =
            if (move.contains('=')) // Example is promotion: gxh8=Q
                pgnCoordinateToIndex("" + move[move.length - 4] + move[move.length - 3])
            else pgnCoordinateToIndex("" + move[move.length - 2] + move[move.length - 1])

        // If pawn move
        if (move[0].isLowerCase()) {
            val col = move[0].code - a_VALUE

            val range =
                if (currentPlayer == Army.BLACK)
                    targetIdx / 8 downTo 0
                else (targetIdx / 8) until SIZE

            for (i in range) {
                val square = getIndex(i,col)

                // In case there's 2 pawns in one col, to not move one back
                if (currentPlayer == Army.WHITE && square < targetIdx) continue
                if (currentPlayer == Army.BLACK && square > targetIdx) continue

                val element = board[square]

                if (element is Pawn && element.checkPieceBounds(element.index, targetIdx) && element.army == currentPlayer) {
                    switchPositions(getIndex(i,col), targetIdx, false)

                    // If promotion
                    if (move.contains('=')) {
                        lastMoved.remove(board[targetIdx])
                        when (move[move.length - 1]) {
                            'N' -> board[targetIdx] = Knight(targetIdx, currentPlayer)
                            'B' -> board[targetIdx] = Bishop(targetIdx, currentPlayer)
                            'R' -> board[targetIdx] = Rook(targetIdx, currentPlayer)
                            'Q' -> board[targetIdx] = Queen(targetIdx, currentPlayer)
                        }
                        board[targetIdx].lastMoved = true
                        lastMoved.add(board[targetIdx])
                    }

                    return
                }
            }
            // Shouldn't be here unless invalid pgn
        }

        // If there as two equal pieces that can move to the target square
        // Notation example: Nfe6 or Nfxe6
        if (move.length == 5 || (move.length == 4 && !move.contains('x'))) {
            val given = move[1]

            val pieceType = when (move[0]) {
                'N' -> Knight::class.java
                'B' -> Bishop::class.java
                'R' -> Rook::class.java
                else -> Queen::class.java
            }
            for (i in 0..7) {
                val index =
                    if (given.isDigit()) // If the row was given
                        getIndex(given.digitToInt() - SIZE, i)
                    else getIndex(i,given.code - a_VALUE) // If the column was given
                if (isOffBounds(index)) continue
                val element = board[index]

                if (element::class.java == pieceType && (element as Piece).army == currentPlayer) {
                    switchPositions(index, targetIdx, false)
                    return
                }
            }
        }

        // Regular piece move
        when (move[0]) {
            'N' -> getPieceAndPlace(targetIdx, Knight.offsets, Knight::class.java)
            'B' -> getLongPieceAndPlace(targetIdx, Bishop.offsets, Bishop::class.java)
            'R' -> getLongPieceAndPlace(targetIdx, Rook.offsets, Rook::class.java)
            'Q' -> getLongPieceAndPlace(targetIdx, Queen.offsets, Queen::class.java)
            'K' -> getPieceAndPlace(targetIdx, King.offsets, King::class.java)
        }
    }

    /**
     * For Kings and Knights, when playing the moves in a pgn.
     * If it's a simple move this function is called.
     *
     * Checks the squares around the target square with a pattern
     * based on the piece. The goal is to find a piece of that type
     * and color and move it to the target square.
     */
    private fun getPieceAndPlace(targetIdx: Int, offsets: List<Int>, type: Type) {
        for (offset in offsets) {
            var square = targetIdx
            square += offset
            if (!isOffBounds(square)) {
                val element = board[square]
                if (element.javaClass == type && (element as Piece).checkPieceBounds(targetIdx, 0) && element.army == currentPlayer) {
                    switchPositions(square, targetIdx, false)
                    return
                }
            }
        }
    }

    /**
     * For Queens, Bishops and Rooks, when playing the moves in a pgn.
     * If it's a simple move this function is called.
     *
     * Checks the squares around the target square with a pattern
     * based on the piece. The goal is to find a piece of that type
     * and color and move it to the target square.
     */
    private fun getLongPieceAndPlace(targetIdx: Int, offsets: List<Int>, type: Class<*>) {
        for ((offsetIdx, offset) in offsets.withIndex()) {
            var square = targetIdx
            square += offset
            while (!isOffBounds(square)) {
                val element = board[square]
                if (element.javaClass == type && (element as Piece).checkPieceBounds(targetIdx, offsetIdx) && element.army == currentPlayer) {
                    switchPositions(square, targetIdx, false)
                    return
                } else if (element is Piece) break
                square += offset
            }
        }
    }

    /**
     * Translates [board] into a string in FEN format.
     * Numbers represent repeated spaces in a row.
     * Letters are pieces, lower-case for black, upper-case for white.
     * Each row is divided by a '/'.
     */
    fun saveToFEN(): String {
        val fen = StringBuilder()

        var spaces = 0
        for ((index, element) in board.withIndex()) {
            if (element is Piece) {
                if (spaces > 0) {
                    fen.append(spaces)
                    spaces = 0
                }

                var letter: Char = when (element) {
                    is Pawn -> 'p'
                    is Knight -> 'n'
                    is Bishop -> 'b'
                    is Rook -> 'r'
                    is Queen -> 'q'
                    else -> 'k' // king
                }

                if (element.army == Army.WHITE)
                    letter = letter.uppercaseChar()

                fen.append(letter)
            } else {
                spaces++
            }

            if (index % SIZE == 7) {
                if (spaces > 0) {
                    fen.append(spaces)
                    spaces = 0
                }
                if (index != 63) // if it's not the last square
                    fen.append('/')
            }
        }

        return fen.toString()
    }

    /**
     * Replaced [board] with the representation given in fen format.
     * Details about FEN explained in the upper method.
     */
    fun loadFromFEN(fen: String) {
        val rows = fen.split('/')

        var index = 0
        for (row in rows) {
            for (letter in row) {
                if (letter.isDigit()) {
                    var spaces = letter.digitToInt()

                    while (spaces-- > 0) {
                        board[index] = Empty(index++)
                    }
                } else {
                    val army = if (letter.isUpperCase()) Army.WHITE else Army.BLACK

                    when (letter.lowercaseChar()) {
                        'p' -> board[index] = Pawn(index++, army)
                        'n' -> board[index] = Knight(index++, army)
                        'b' -> board[index] = Bishop(index++, army)
                        'r' -> board[index] = Rook(index++, army)
                        'q' -> board[index] = Queen(index++, army)
                        'k' -> {
                            board[index] = King(index, army)
                            if (army == Army.WHITE)
                                whiteKing = board[index] as King
                            else blackKing = board[index] as King
                            index++
                        }
                    }
                }
            }
        }

        calculatePossibleMoves()
    }
}