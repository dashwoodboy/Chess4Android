package pt.isel.pdm.chess4android.onlinegame.challenge.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.pdm.chess4android.common.ChessApplication
import pt.isel.pdm.chess4android.onlinegame.challenge.ChallengeInfo
import pt.isel.pdm.chess4android.onlinegame.game.GameState

class ChallengeListViewModel(app: Application) : AndroidViewModel(app) {

    private val app = getApplication<ChessApplication>()

    private val _challenges: MutableLiveData<List<ChallengeInfo>> = MutableLiveData()
    val challenges: LiveData<List<ChallengeInfo>> = _challenges

    fun fetchChallenges() {
        viewModelScope.launch(Dispatchers.IO) {
            _challenges.postValue(app.challengesRepository.fetchChallenges())
        }

    }

    private val _enrolmentResult: MutableLiveData<Result<Pair<ChallengeInfo, GameState>>?> = MutableLiveData()
    val enrolmentResult: LiveData<Result<Pair<ChallengeInfo, GameState>>?> = _enrolmentResult

    fun tryAcceptChallenge(challengeInfo: ChallengeInfo) {
        val app = getApplication<ChessApplication>()

        app.challengesRepository.withdrawChallenge(
            challengeId = challengeInfo.id,
            onComplete = {
                it.onSuccess {

                    app.onlineGameRepository.createGame(challengeInfo, onComplete = { game ->
                        _enrolmentResult.value = game
                    })
                }
            }
        )
    }
}