package hr.markopetricevic.weatherapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import hr.markopetricevic.weatherapp.data.room.AppDatabase
import hr.markopetricevic.weatherapp.data.room.FavoritesDao
import hr.markopetricevic.weatherapp.data.room.FavoritesEntry
import hr.markopetricevic.weatherapp.data.room.SearchHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val database: AppDatabase,
    private val favoritesDao: FavoritesDao
) {
    enum class ThemeMode {
        LIGHT, DARK
    }

    enum class Language(val code: String) {
        HR("hr"), EN("en"), DE("de")
    }

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("language")
    private val lastCityKey = stringPreferencesKey("last_city")

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val mode = preferences[themeModeKey] ?: ThemeMode.LIGHT.name
        ThemeMode.valueOf(mode)
    }

    val language: Flow<Language> = dataStore.data.map { preferences ->
        val lang = preferences[languageKey] ?: Language.HR.name
        Language.valueOf(lang)
    }

    val lastCity: Flow<String?> = dataStore.data.map { preferences ->
        preferences[lastCityKey]
    }

    val searchHistory: Flow<List<String>> = database.searchHistoryDao().getSearchHistory().map { entries ->
        entries.map { it.city.uppercase() }.distinct()
    }

    val favorites: Flow<List<String>> = favoritesDao.getFavorites().map { entries ->
        entries.map { it.city }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }

    suspend fun setLanguage(lang: Language) {
        dataStore.edit { preferences ->
            preferences[languageKey] = lang.name
        }
    }

    suspend fun setLastCity(city: String) {
        if (isValidCity(city)) {
            dataStore.edit { preferences ->
                preferences[lastCityKey] = city
            }
        }
    }

    suspend fun addToSearchHistory(city: String) {
        if (isValidCity(city)) {
            database.searchHistoryDao().insert(SearchHistoryEntry(city = city))
        }
    }

    suspend fun addToFavorites(city: String) {
        if (isValidCity(city)) {
            favoritesDao.insert(FavoritesEntry(city = city))
        }
    }

    suspend fun removeFromFavorites(city: String) {
        favoritesDao.delete(city)
    }

    private fun isValidCity(city: String): Boolean {
        val trimmedCity = city.trim()
        return trimmedCity.isNotBlank()
    }
}