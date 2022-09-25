package pt.isel.pdm.chess4android.daily

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import pt.isel.pdm.chess4android.common.ChessApplication

/**
 * Definition of the background job that fetches the daily puzzle and stores it in the history DB.
 */
class DownloadDailyPuzzleWorker (appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app : ChessApplication = applicationContext as ChessApplication
        app.puzzlesRepository.fetchPuzzleOfDay()

        return Result.success()
    }
}