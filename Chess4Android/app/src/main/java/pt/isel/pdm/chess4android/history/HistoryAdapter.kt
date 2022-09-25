package pt.isel.pdm.chess4android.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.common.PuzzleDTO

/**
 * Implementation of the ViewHolder pattern. Its purpose is to eliminate the need for
 * executing findViewById each time a reference to a view's child is required.
 */
class HistoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val dateView = itemView.findViewById<TextView>(R.id.date)
    private val completedView = itemView.findViewById<ImageView>(R.id.completed)

    private val incompletePuzzleDrawable = AppCompatResources.getDrawable(itemView.context, R.drawable.red_cross_24)
    private val completePuzzleDrawable = AppCompatResources.getDrawable(itemView.context, R.drawable.green_check_24)

    /**
     * Binds this view holder to the given puzzle item
     */
    fun bindTo(puzzleDTO: PuzzleDTO, onItemClick: () -> Unit) {
        dateView.text = puzzleDTO.date
        val completedStatusImage =
            if (puzzleDTO.completed == 0) incompletePuzzleDrawable else completePuzzleDrawable
        completedView.setImageDrawable(completedStatusImage)
        itemView.setOnClickListener {
            onItemClick()
        }
    }
}

/**
 * Adapts items in a data set to RecycleView entries
 */
class HistoryAdapter(
    private val dataSource: List<PuzzleDTO>,
    private val onItemClick: (PuzzleDTO) -> Unit
) : RecyclerView.Adapter<HistoryItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_view, parent, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bindTo(dataSource[position]) {
            onItemClick(dataSource[position])
        }
    }

    override fun getItemCount() = dataSource.size
}