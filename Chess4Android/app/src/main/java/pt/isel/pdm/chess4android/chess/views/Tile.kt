package pt.isel.pdm.chess4android.chess.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.chess.*

/**
 * Custom view that implements a chess board tile.
 * Tiles are either black or white and can they can be empty or occupied by a chess piece.
 *
 * Implementation note: This view is not to be used with the designer tool.
 * You need to adapt this view to suit your needs. ;)
 *
 * @property type           The tile's type (i.e. black or white)
 * @property tilesPerSide   The number of tiles in each side of the chess board
 *
 */
@SuppressLint("ViewConstructor")
class Tile(
    private val ctx: Context,
    private val type: Type,
    private val tilesPerSide: Int,
    private val images: Map<Pair<Class<out Piece>, Army>, VectorDrawableCompat?>
) : View(ctx) {

    var element: Element = Empty(0)
        set(value) {
            field = value
            this.invalidate()
        }

    enum class Type { WHITE, BLACK }

    private val squarePaint = Paint().apply {
        color = ctx.resources.getColor(
            if (type == Type.WHITE) R.color.chess_board_white else R.color.chess_board_black,
            null
        )
        style = Paint.Style.FILL_AND_STROKE
    }

    private val possibleMovesSquarePaint = Paint().apply {
        color = ctx.resources.getColor(R.color.selected_piece_green, null)
        style = Paint.Style.FILL_AND_STROKE
    }

    private val lastMoveSquarePaint = Paint().apply {
        color = ctx.resources.getColor(R.color.last_move_yellow, null)
        style = Paint.Style.FILL_AND_STROKE
    }

    private val kingCheckedPaint = Paint().apply {
        color = ctx.resources.getColor(R.color.king_check_red, null)
        style = Paint.Style.FILL_AND_STROKE
    }

    private var halfSquareSize: Float = 0f
    private var emptySquareTargetSize: Float = 0f
    private var enemyPieceTargetSize: Float = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val side = Integer.min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        val measuredSize = side / tilesPerSide

        halfSquareSize = measuredSize / 2f
        emptySquareTargetSize = measuredSize / 8f
        enemyPieceTargetSize = measuredSize / 1.8f

        setMeasuredDimension(measuredSize, measuredSize)
    }

    override fun onDraw(canvas: Canvas) {
        if (element is Piece) {
            val pieceAux = element as Piece
            when {
                pieceAux.selected -> canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    possibleMovesSquarePaint
                )
                pieceAux.isTarget -> {
                    canvas.drawRect(
                        0f,
                        0f,
                        width.toFloat(),
                        height.toFloat(),
                        possibleMovesSquarePaint
                    )
                    canvas.drawCircle(
                        halfSquareSize,
                        halfSquareSize,
                        enemyPieceTargetSize,
                        squarePaint
                    )
                }
                pieceAux.lastMoved -> canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    lastMoveSquarePaint
                )
                else -> canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), squarePaint)
            }

            if (element is King) {
                if ((element as King).checked) {
                    canvas.drawCircle(
                        halfSquareSize,
                        halfSquareSize,
                        enemyPieceTargetSize,
                        kingCheckedPaint
                    )
                }
            }

            images[Pair(pieceAux::class.java, pieceAux.army)]?.apply {
                val padding = 8
                setBounds(padding, padding, width - padding, height - padding)
                draw(canvas)
            }
        } else {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), squarePaint)
            if (element.isTarget)
                canvas.drawCircle(
                    halfSquareSize,
                    halfSquareSize,
                    emptySquareTargetSize,
                    possibleMovesSquarePaint
                )
            else if (element.lastMoved)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), lastMoveSquarePaint)
        }
    }
}