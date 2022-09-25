package pt.isel.pdm.chess4android.onlinegame.challenge.list

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.onlinegame.challenge.ChallengeInfo

class ChallengeViewHolder(private val view: ViewGroup) : RecyclerView.ViewHolder(view) {

    private val challengerNameView: TextView = view.findViewById(R.id.challengerName)
    private val challengerMessageView: TextView = view.findViewById(R.id.message)

    fun bindTo(challenge: ChallengeInfo?, itemSelectedListener: (ChallengeInfo) -> Unit) {
        challengerNameView.text = challenge?.challengerName ?: ""
        challengerMessageView.text = challenge?.challengerMessage ?: ""

        if (challenge != null)
            view.setOnClickListener {
                itemView.isClickable = false
                itemSelectedListener(challenge)
                itemView.isClickable = true
            }
    }
}

class ChallengesListAdapter(
    private val contents: List<ChallengeInfo> = emptyList(),
    private val itemSelectedListener: (ChallengeInfo) -> Unit = { }
) :
    RecyclerView.Adapter<ChallengeViewHolder>() {

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bindTo(contents[position], itemSelectedListener)
    }

    override fun getItemCount(): Int = contents.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false) as ViewGroup

        return ChallengeViewHolder(view)
    }
}