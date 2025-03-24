package hr.markopetricevic.weatherapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Insert
    suspend fun insert(entry: FavoritesEntry)

    @Query("SELECT * FROM favorites")
    fun getFavorites(): Flow<List<FavoritesEntry>>

    @Query("DELETE FROM favorites WHERE city = :city")
    suspend fun delete(city: String)
}