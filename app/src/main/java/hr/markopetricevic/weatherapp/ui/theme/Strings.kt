package hr.markopetricevic.weatherapp.ui.theme

import hr.markopetricevic.weatherapp.data.AppPreferences

object Strings {
    private val hr = mapOf(
        "enter_city_name" to "Unesite naziv grada",
        "get_weather" to "Dohvati vrijeme",
        "recent_searches" to "Nedavne pretrage",
        "no_favorites" to "Još nema omiljenih gradova.",
        "three_day_forecast" to "3-dnevna prognoza (3-satni intervali)",
        "feels_like" to "Osjećaj: %s°C",
        "humidity" to "Vlažnost: %s%%",
        "wind" to "Vjetar: %s m/s",
        "sunrise" to "Izlazak sunca: %s 🌅",
        "sunset" to "Zalazak sunca: %s 🌇",
        "rain" to "Kiša: %s mm",
        "snow" to "Snijeg: %s mm",
        "enter_city_to_see_weather" to "Unesite grad da biste vidjeli vrijeme",
        "please_enter_city" to "Molimo unesite naziv grada da biste vidjeli prognozu",
        "no_forecast_data" to "Nema dostupnih podataka o prognozi.",
        "loading_forecast" to "Učitavanje prognoze...",
        "language_hr" to "Hrvatski",
        "language_en" to "Engleski",
        "language_de" to "Njemački",
        "favorites" to "Omiljeni gradovi",
        "today" to "Danas",
        "tomorrow" to "Sutra",
        "remove_from_favorites" to "Ukloni iz omiljenih",
        "add_to_favorites" to "Dodaj u omiljene",
        "open_favorites" to "Otvori omiljene",
        "switch_to_dark_mode" to "Prebaci na tamni način",
        "switch_to_light_mode" to "Prebaci na svijetli način",
        "five_day_forecast" to "5-dnevna prognoza"
    )

    private val en = mapOf(
        "enter_city_name" to "Enter city name",
        "get_weather" to "Get Weather",
        "recent_searches" to "Recent Searches",
        "no_favorites" to "No favorite cities yet.",
        "three_day_forecast" to "3-Day Forecast (3-Hour Intervals)",
        "feels_like" to "Feels like: %s°C",
        "humidity" to "Humidity: %s%%",
        "wind" to "Wind: %s m/s",
        "sunrise" to "Sunrise: %s 🌅",
        "sunset" to "Sunset: %s 🌇",
        "rain" to "Rain: %s mm",
        "snow" to "Snow: %s mm",
        "enter_city_to_see_weather" to "Enter a city to see the weather",
        "please_enter_city" to "Please enter a city name to see the forecast",
        "no_forecast_data" to "No forecast data available.",
        "loading_forecast" to "Loading forecast...",
        "language_hr" to "Croatian",
        "language_en" to "English",
        "language_de" to "German",
        "favorites" to "Favorites",
        "today" to "Today",
        "tomorrow" to "Tomorrow",
        "remove_from_favorites" to "Remove from Favorites",
        "add_to_favorites" to "Add to Favorites",
        "open_favorites" to "Open Favorites",
        "switch_to_dark_mode" to "Switch to Dark Mode",
        "switch_to_light_mode" to "Switch to Light Mode",
        "five_day_forecast" to "5-Day Forecast"

    )

    private val de = mapOf(
        "enter_city_name" to "Stadtname eingeben",
        "get_weather" to "Wetter abrufen",
        "recent_searches" to "Letzte Suchen",
        "no_favorites" to "Noch keine Lieblingsstädte.",
        "three_day_forecast" to "3-Tage-Vorhersage (3-Stunden-Intervalle)",
        "feels_like" to "Gefühlt: %s°C",
        "humidity" to "Luftfeuchtigkeit: %s%%",
        "wind" to "Wind: %s m/s",
        "sunrise" to "Sonnenaufgang: %s 🌅",
        "sunset" to "Sonnenuntergang: %s 🌇",
        "rain" to "Regen: %s mm",
        "snow" to "Schnee: %s mm",
        "enter_city_to_see_weather" to "Geben Sie eine Stadt ein, um das Wetter zu sehen",
        "please_enter_city" to "Bitte geben Sie einen Stadtnamen ein, um die Vorhersage zu sehen",
        "no_forecast_data" to "Keine Vorhersagedaten verfügbar.",
        "loading_forecast" to "Vorhersage wird geladen...",
        "language_hr" to "Kroatisch",
        "language_en" to "Englisch",
        "language_de" to "Deutsch",
        "favorites" to "Favoriten",
        "today" to "Heute",
        "tomorrow" to "Morgen",
        "remove_from_favorites" to "Aus Favoriten entfernen",
        "add_to_favorites" to "Zu Favoriten hinzufügen",
        "open_favorites" to "Favoriten öffnen",
        "switch_to_dark_mode" to "Zum Dunkelmodus wechseln",
        "switch_to_light_mode" to "Zum Hellmodus wechseln",
        "five_day_forecast" to "Fünf-Tage-Vorhersage"
    )

    fun getString(key: String, language: AppPreferences.Language, vararg args: Any): String {
        val translations = when (language) {
            AppPreferences.Language.HR -> hr
            AppPreferences.Language.EN -> en
            AppPreferences.Language.DE -> de
        }
        val template = translations[key] ?: en[key] ?: key
        return if (args.isNotEmpty()) {
            String.format(template, *args)
        } else {
            template
        }
    }
}