package pt.isel.pdm.chess4android.onlinegame.challenge.create

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.chess.Army
import pt.isel.pdm.chess4android.databinding.ActivityChallengeCreateBinding
import pt.isel.pdm.chess4android.onlinegame.game.GameState
import pt.isel.pdm.chess4android.onlinegame.game.OnlineGameActivity

private const val GAME_STATE = "GameState"
private const val PLAYER_COLOR = "PlayerColor"

class ChallengeCreateActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityChallengeCreateBinding.inflate(layoutInflater)
    }

    private val viewModel: ChallengeCreateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.created.observe(this) {
            if (it == null) displayCreateChallenge()
            else it.onFailure { displayError() }.onSuccess {
                displayWaitingForChallenger()
            }
        }

        viewModel.accepted.observe(this) {
            if (it == true) {
                viewModel.created.value?.onSuccess { challenge ->
                    val intent = Intent(this, OnlineGameActivity::class.java)
                    intent.putExtra(GAME_STATE, GameState(challenge.id, Army.WHITE, ""))
                    intent.putExtra(PLAYER_COLOR, Army.WHITE.ordinal)
                    finish()
                    startActivity(intent)
                }
            }
        }

        binding.action.setOnClickListener {
            if (viewModel.created.value == null)
                viewModel.createChallenge(
                    binding.name.text.toString(),
                    binding.message.text.toString()
                )
            else viewModel.removeChallenge()
        }
    }

    private fun displayCreateChallenge() {
        binding.action.text = getString(R.string.create_button_text)
        with(binding.name) { text.clear(); isEnabled = true }
        with(binding.message) { text.clear(); isEnabled = true }
        binding.loading.isVisible = false
        binding.waitingMessage.isVisible = false
    }

    private fun displayWaitingForChallenger() {
        binding.action.text = getString(R.string.waiting_button_text)
        binding.name.isEnabled = false
        binding.message.isEnabled = false
        binding.loading.isVisible = true
        binding.waitingMessage.isVisible = true
    }

    private fun displayError() {
        displayCreateChallenge()
        Toast
            .makeText(this, R.string.error_challenger_fetch, Toast.LENGTH_LONG)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeChallenge()
    }
}