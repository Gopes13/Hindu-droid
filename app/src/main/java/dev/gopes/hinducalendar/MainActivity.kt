package dev.gopes.hinducalendar

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.core.navigation.NavGraph
import dev.gopes.hinducalendar.data.service.InAppReviewHelper
import dev.gopes.hinducalendar.core.ui.theme.HinduCalendarTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var inAppReviewHelper: InAppReviewHelper

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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

        checkForUpdates()
        lifecycleScope.launch { inAppReviewHelper.maybeRequestReview(this@MainActivity) }

        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val vibrantMode by preferencesRepository.preferencesFlow
                .map { it.gamificationData.isEnabled }
                .collectAsState(initial = false)

            HinduCalendarTheme(vibrantMode = vibrantMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(windowSizeClass = windowSizeClass)
                }
            }
        }
    }

    private fun checkForUpdates() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlow(
                    info,
                    this,
                    AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)
                )
            }
        }.addOnFailureListener { e ->
            Timber.w(e, "Update check failed")
        }
    }
}
