package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.MainActivity
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AppTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Inject lateinit var trackRepositoryImpl: TrackRepositoryImpl

  @Before fun setup() {}

  @Test
  fun canNavigateToLaunchScreen() = run {
    onComposeScreen<AppScreen>(composeTestRule) {
      launchScreenButton {
        assertIsDisplayed()
        performClick()
      }
      launchScreen { assertIsDisplayed() }
    }
  }

  @Test
  fun canNavigateToLoginScreen() = run {
    onComposeScreen<AppScreen>(composeTestRule) {
      loginScreenButton {
        assertIsDisplayed()
        performClick()
      }
      loginScreen { assertIsDisplayed() }
    }
  }

  @Test
  fun canNavigateToTrackListScreen() = run {
    onComposeScreen<AppScreen>(composeTestRule) {
      trackListScreenButton {
        assertIsDisplayed()
        performClick()
      }
      trackListScreen { assertIsDisplayed() }
    }
  }
}
