package hr.markopetricevic.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import hr.markopetricevic.weatherapp.data.AppPreferences
import hr.markopetricevic.weatherapp.navigation.SetupNavGraph
import hr.markopetricevic.weatherapp.ui.theme.WeatherAppTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            WeatherAppTheme(appPreferences = appPreferences) {
                val navController = rememberNavController()
                SetupNavGraph(
                    navController = navController,
                    appPreferences = appPreferences
                )
            }
        }
    }
}