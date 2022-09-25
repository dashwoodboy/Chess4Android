package pt.isel.pdm.chess4android.onlinegame.challenge

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private const val CHALLENGES_COLLECTION = "challenges"

private const val CHALLENGER_NAME = "challengerName"
private const val CHALLENGER_MESSAGE = "challengerMessage"

class ChallengesRepository {

    suspend fun fetchChallenges(): List<ChallengeInfo> {
        val limit = 30
        return Firebase.firestore.collection(CHALLENGES_COLLECTION)
            .get()
            .await()
            .documents
            .take(limit)
            .map {
                it.toChallengeInfo()
            }
    }

    fun publishChallenge(
        name: String,
        message: String,
        onComplete: (Result<ChallengeInfo>) -> Unit
    ) {
        Firebase.firestore.collection(CHALLENGES_COLLECTION)
            .add(hashMapOf(CHALLENGER_NAME to name, CHALLENGER_MESSAGE to message))
            .addOnSuccessListener {
                onComplete(Result.success(ChallengeInfo(it.id, name, message)))
            }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    fun subscribeToChallengeAcceptance(
        challengeId: String,
        onSubscriptionError: (Exception) -> Unit,
        onChallengeAccepted: () -> Unit
    ): ListenerRegistration {

        return Firebase.firestore
            .collection(CHALLENGES_COLLECTION)
            .document(challengeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onSubscriptionError(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == false) {
                    // Document has been removed, thereby signalling that someone accepted
                    // the challenge
                    onChallengeAccepted()
                }
            }
    }

    fun withdrawChallenge(challengeId: String, onComplete: (Result<Unit>) -> Unit) {
        Firebase.firestore
            .collection(CHALLENGES_COLLECTION)
            .document(challengeId)
            .delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    private fun DocumentSnapshot.toChallengeInfo() =
        ChallengeInfo(
            id,
            data?.get(CHALLENGER_NAME) as String,
            data?.get(CHALLENGER_MESSAGE) as String
        )

    fun unsubscribeToChallengeAcceptance(subscription: ListenerRegistration) {
        subscription.remove()
    }
}