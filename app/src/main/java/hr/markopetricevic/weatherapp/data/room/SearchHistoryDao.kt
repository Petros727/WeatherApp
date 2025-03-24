package hr.markopetricevic.weatherapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Insert
    suspend fun insert(entry: SearchHistoryEntry)

    @Query("SELECT * FROM search_history ORDER BY id DESC LIMIT 10")
    fun getSearchHistory(): Flow<List<SearchHistoryEntry>>
}