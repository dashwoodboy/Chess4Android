package pt.isel.pdm.chess4android.common

import android.app.Application
import androidx.room.Room
import androidx.work.*
import pt.isel.pdm.chess4android.daily.DownloadDailyPuzzleWorker
import pt.isel.pdm.chess4android.history.HistoryDatabase
import pt.isel.pdm.chess4android.onlinegame.challenge.ChallengesRepository
import pt.isel.pdm.chess4android.onlinegame.game.OnlineGameRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.Gson

/**
 * We use the application object for resolving dependencies with global significance.
 */
class ChessApplication : Application() {

    /**
     * The service used to reach the Lichess API that provides daily puzzles.
     */
    private val puzzleOfDayService: PuzzleOfDayService by lazy {
        Retrofit.Builder()
            .baseUrl("https://lichess.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PuzzleOfDayService::class.java)
    }

    /**
     * Database containing the puzzles of the day
     */
    private val historyDB : HistoryDatabase by lazy {
        Room
            .databaseBuilder(this, HistoryDatabase::class.java, "DailyPuzzles 2.3")
            .build()
    }

    val puzzlesRepository : PuzzleRepository by lazy {
        PuzzleRepository(puzzleOfDayService, historyDB.getHistoryPuzzleDao())
    }

    val challengesRepository: ChallengesRepository by lazy {
        ChallengesRepository()
    }

    val onlineGameRepository: OnlineGameRepository by lazy {
        OnlineGameRepository(mapper)
    }

    private val mapper: Gson by lazy { Gson() }

    /**
     * Called each time the application process is loaded
     */
    override fun onCreate() {
        super.onCreate()

        val workRequest = PeriodicWorkRequestBuilder<DownloadDailyPuzzleWorker>(4, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "DownloadDailyPuzzle",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }
}