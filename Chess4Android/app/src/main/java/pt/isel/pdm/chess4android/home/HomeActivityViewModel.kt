package pt.isel.pdm.chess4android.home

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.pdm.chess4android.common.ChessApplication
import pt.isel.pdm.chess4android.common.PuzzleRepository
import pt.isel.pdm.chess4android.common.PuzzleOfDay

class HomeActivityViewModel(application: Application) : AndroidViewModel(application) {

    private var puzzleRepository: PuzzleRepository = getApplication<ChessApplication>().puzzlesRepository
    private val puzzleOfDay by lazy { MutableLiveData<PuzzleOfDay>() }

    fun getPuzzleOfDay(): LiveData<PuzzleOfDay> {
        if (puzzleOfDay.value != null)
            return puzzleOfDay
        viewModelScope.launch(Dispatchers.IO) {
            puzzleOfDay.postValue(puzzleRepository.fetchPuzzleOfDay())
        }
        return puzzleOfDay
    }
}