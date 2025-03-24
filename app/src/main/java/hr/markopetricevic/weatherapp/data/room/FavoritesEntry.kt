package hr.markopetricevic.weatherapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoritesEntry(
    @PrimaryKey val city: String
)