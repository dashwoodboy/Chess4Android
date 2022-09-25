package pt.isel.pdm.chess4android.onlinegame.game

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import pt.isel.pdm.chess4android.chess.Army
import pt.isel.pdm.chess4android.onlinegame.challenge.ChallengeInfo

private const val GAMES_COLLECTION = "games"

private const val GAME_STATE_KEY = "game"

/**
 * Repository for Online Game, responsible to update state and subscribe to changes on Firestore
 */
class OnlineGameRepository(private val mapper: Gson) {

    /**
     * Create a game on Firestore containing the GameState instance in json format
     */
    fun createGame(
        challenge: ChallengeInfo,
        onComplete: (Result<Pair<ChallengeInfo, GameState>>) -> Unit
    ) {
        val gameState = GameState(challenge.id, Army.WHITE, "")
        Firebase.firestore.collection(GAMES_COLLECTION)
            .document(challenge.id)
            .set(hashMapOf(GAME_STATE_KEY to mapper.toJson(gameState)))
            .addOnSuccessListener { onComplete(Result.success(Pair(challenge, gameState))) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    /**
     * Updates GameState already existent on Firestore
     */
    fun updateGameState(gameState: GameState, onComplete: (Result<GameState>) -> Unit) {
        Firebase.firestore.collection(GAMES_COLLECTION)
            .document(gameState.id)
            .set(hashMapOf(GAME_STATE_KEY to mapper.toJson(gameState)))
            .addOnSuccessListener { onComplete(Result.success(gameState)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    /**
     * Subscribe to changes at a specific document on Firestore using SnapshotListener
     */
    fun subscribeToGameStateChanges(
        challengeId: String,
        onSubscriptionError: (Exception) -> Unit,
        onGameStateChange: (GameState) -> Unit
    ): ListenerRegistration {

        return Firebase.firestore
            .collection(GAMES_COLLECTION)
            .document(challengeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onSubscriptionError(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    val gameState = mapper.fromJson(
                        snapshot.get(GAME_STATE_KEY) as String,
                        GameState::class.java
                    )
                    onGameStateChange(gameState)
                }
            }
    }

    /**
     * Deletes the shared game state for the given challenge.
     */
    fun deleteGame(challengeId: String, onComplete: (Result<Unit>) -> Unit) {
        Firebase.firestore.collection(GAMES_COLLECTION)
            .document(challengeId)
            .delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }
}