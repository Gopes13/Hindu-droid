package dev.gopes.hinducalendar

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.navigation.NavGraph
import dev.gopes.hinducalendar.ui.theme.HinduCalendarTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sync stored language preference with AppCompat locale on startup
        lifecycleScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            val tag = when (prefs.language) {
                AppLanguage.ENGLISH, AppLanguage.HINGLISH -> "en"
                else -> prefs.language.code
            }
            val current = AppCompatDelegate.getApplicationLocales()
            if (current.isEmpty || current.toLanguageTags() != tag) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(tag)
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            HinduCalendarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}
