package pt.isel.pdm.chess4android.onlinegame.challenge.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import pt.isel.pdm.chess4android.common.ChessApplication
import pt.isel.pdm.chess4android.onlinegame.challenge.ChallengeInfo

class ChallengeCreateViewModel(app: Application) : AndroidViewModel(app) {


    private val _created: MutableLiveData<Result<ChallengeInfo>?> = MutableLiveData(null)
    val created: LiveData<Result<ChallengeInfo>?> = _created


    private val _accepted: MutableLiveData<Boolean> = MutableLiveData(false)
    val accepted: LiveData<Boolean> = _accepted


    fun createChallenge(name: String, message: String) {
        getApplication<ChessApplication>().challengesRepository.publishChallenge(
            name = name,
            message = message,
            onComplete = {
                _created.value = it
                it.onSuccess(::waitForAcceptance)
            }
        )
    }

    fun removeChallenge() {
        val currentChallenge = created.value
        if (currentChallenge == null || currentChallenge.isFailure)
            return
        val repo = getApplication<ChessApplication>().challengesRepository
        subscription?.let { repo.unsubscribeToChallengeAcceptance(it) }
        currentChallenge.onSuccess {
            repo.withdrawChallenge(
                challengeId = it.id,
                onComplete = { _created.value = null }
            )
        }
    }

    private var subscription: ListenerRegistration? = null

    private fun waitForAcceptance(challengeInfo: ChallengeInfo) {
        subscription =
            getApplication<ChessApplication>().challengesRepository.subscribeToChallengeAcceptance(
                challengeId = challengeInfo.id,
                onSubscriptionError = { _created.value = Result.failure(it) },
                onChallengeAccepted = { _accepted.value = true },
            )
    }
}