package pt.isel.pdm.chess4android.onlinegame.challenge.list

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.chess.Army
import pt.isel.pdm.chess4android.databinding.ActivityChallengesListBinding
import pt.isel.pdm.chess4android.onlinegame.challenge.ChallengeInfo
import pt.isel.pdm.chess4android.onlinegame.challenge.create.ChallengeCreateActivity
import pt.isel.pdm.chess4android.onlinegame.game.OnlineGameActivity

private const val GAME_STATE = "GameState"
private const val PLAYER_COLOR = "PlayerColor"

class ChallengesListActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityChallengesListBinding.inflate(layoutInflater)
    }

    private val viewModel: ChallengeListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.challengesList.setHasFixedSize(true)
        binding.challengesList.layoutManager = LinearLayoutManager(this)
        binding.createChallengeButton.setOnClickListener {
            startActivity(Intent(this, ChallengeCreateActivity::class.java))
        }
        binding.refreshLayout.setOnRefreshListener { updateChallengesList() }

        viewModel.challenges.observe(this) {
            if (it != null) {
                binding.challengesList.adapter = ChallengesListAdapter(it, ::challengeSelected)
                binding.refreshLayout.isRefreshing = false
            } else {
                Toast.makeText(this, R.string.error_challenger_fetch, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.enrolmentResult.observe(this) { result ->
            result?.onSuccess {
                val intent = Intent(this, OnlineGameActivity::class.java)
                intent.putExtra(GAME_STATE, it.second)
                intent.putExtra(PLAYER_COLOR, Army.BLACK.ordinal)
                startActivity(intent)

            }
        }
        updateChallengesList()
    }

    private fun updateChallengesList() {
        binding.refreshLayout.isRefreshing = true
        viewModel.fetchChallenges()
    }

    private fun challengeSelected(challenge: ChallengeInfo) {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.alertdialog_title_text, challenge.challengerName))
            .setPositiveButton(getString(R.string.alertdialog_positive_text)) { _, _ ->
                viewModel.tryAcceptChallenge(
                    challenge
                )
            }
            .setNegativeButton(getString(R.string.alertdialog_negative_text), null)
            .create()
            .show()
    }
}