package hr.markopetricevic.weatherapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String
)