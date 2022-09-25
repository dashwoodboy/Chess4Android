package pt.isel.pdm.chess4android.history

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.pdm.chess4android.common.ChessApplication
import pt.isel.pdm.chess4android.common.PuzzleRepository

import pt.isel.pdm.chess4android.common.PuzzleDTO

/**
 * The actual execution host behind the puzzle history screen (i.e. the [HistoryActivity]).
 */
class HistoryActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : PuzzleRepository = getApplication<ChessApplication>().puzzlesRepository

    /**
     * Gets the puzzle list (history) from the DB.
     */
    fun getHistory(): LiveData<List<PuzzleDTO>> {
        val publish = MutableLiveData<List<PuzzleDTO>>()
        try {
            viewModelScope.launch(Dispatchers.IO) {
                publish.postValue(repository.getAllHistory())
            }
        } catch (e: Throwable) {
            publish.postValue(null)
        }

        return publish
    }
}