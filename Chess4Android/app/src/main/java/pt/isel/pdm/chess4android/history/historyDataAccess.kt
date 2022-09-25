package pt.isel.pdm.chess4android.history

import androidx.room.*

/**
 * The data type that represents data stored in the "history_puzzle" table of the DB
 */
@Entity(tableName = "history_puzzle")
data class PuzzleEntity(
    @PrimaryKey val id: String,
    val pgn: String,
    val puzzle: String,
    val completed: Int,
    val date: String
)

/**
 * The abstraction containing the supported data access operations. The actual implementation is
 * provided by the Room compiler. We can have as many DAOs has our design mandates.
 */
@Dao
interface HistoryPuzzleDao {
    @Insert
    suspend fun insert(puzzle: PuzzleEntity)

    @Delete
    suspend fun delete(puzzle: PuzzleEntity)

    @Query("SELECT * FROM history_puzzle ORDER BY date DESC LIMIT 100")
    suspend fun getALl() : List<PuzzleEntity>

    @Query("SELECT * FROM history_puzzle ORDER BY date DESC LIMIT :count")
    suspend fun getLast(count: Int): List<PuzzleEntity>

    @Query("UPDATE history_puzzle SET completed = 1 WHERE date = :date")
    suspend fun setPuzzleToCompleted(date: String)
}

/**
 * The abstraction that represents the DB itself.
 */
@Database(entities = [PuzzleEntity::class], version = 1)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun getHistoryPuzzleDao(): HistoryPuzzleDao
}