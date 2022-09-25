package pt.isel.pdm.chess4android.chess.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.GridLayout
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.chess.*
import pt.isel.pdm.chess4android.chess.views.Tile.Type

typealias TileTouchListener = (row: Int, column: Int) -> Unit

/**
 * Custom view that implements a chess board.
 */
@SuppressLint("ClickableViewAccessibility")
class BoardView(private val ctx: Context, attrs: AttributeSet?) : GridLayout(ctx, attrs) {

    private val side = 8

    private val brush = Paint().apply {
        ctx.resources.getColor(R.color.chess_board_black, null)
        style = Paint.Style.STROKE
        strokeWidth = 10F
    }

    private fun createImageEntry(imageId: Int) =
        VectorDrawableCompat.create(ctx.resources, imageId, null)

    private val piecesImages = mapOf(
        Pair(Pawn::class.java, Army.WHITE) to createImageEntry(R.drawable.ic_white_pawn),
        Pair(Knight::class.java, Army.WHITE) to createImageEntry(R.drawable.ic_white_knight),
        Pair(Bishop::class.java, Army.WHITE) to createImageEntry(R.drawable.ic_white_bishop),
        Pair(Rook::class.java, Army.WHITE) to createImageEntry(R.drawable.ic_white_rook),
        Pair(Queen::class.java, Army.WHITE) to createImageEntry(R.drawable.ic_white_queen),
        Pair(King::class.java, Army.WHITE) to createImageEntry(R.drawable.ic_white_king),
        Pair(Pawn::class.java, Army.BLACK) to createImageEntry(R.drawable.ic_black_pawn),
        Pair(Knight::class.java, Army.BLACK) to createImageEntry(R.drawable.ic_black_knight),
        Pair(Bishop::class.java, Army.BLACK) to createImageEntry(R.drawable.ic_black_bishop),
        Pair(Rook::class.java, Army.BLACK) to createImageEntry(R.drawable.ic_black_rook),
        Pair(Queen::class.java, Army.BLACK) to createImageEntry(R.drawable.ic_black_queen),
        Pair(King::class.java, Army.BLACK) to createImageEntry(R.drawable.ic_black_king),
    )

    private var tiles: MutableList<Tile>
    var onTileClickedListener: TileTouchListener? = null
    private var pov = Army.WHITE

    init {
        rowCount = side
        columnCount = side
        tiles = MutableList(side * side) {
            val row = it / side
            val column = it % side
            val tile = Tile(
                ctx,
                if ((row + column) % 2 == 0) Type.WHITE else Type.BLACK,
                side,
                piecesImages
            )
            tile.setOnClickListener { onTileClickedListener?.invoke(row, column) }
            addView(tile)
            tile
        }
    }

    /**
     * Set all tiles to new board.
     */
    fun setBoard(board: List<Element>) {
        for (i in tiles.indices) {
            tiles[i].element = board[i]
        }
    }

    /**
     * Flip the board the given [pov].
     */
    fun flip(pov: Army) {
        if (this.pov == pov) return

        this.pov = pov

        for (i in 0 until (tiles.size / 2)) {
            val view1 = getChildAt(i)
            val view2 = getChildAt(tiles.size - 1 - i)
            removeView(view1)
            removeView(view2)
            addView(view1, tiles.size - 2 - i)
            addView(view2, i)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, brush)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), brush)
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), brush)
        canvas.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), brush)
    }
}