package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainPlaceHolderTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule val intentsRule = IntentsRule()

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK private lateinit var mockProfileViewModel: ProfileViewModel

  @Before
  fun setup() {
    composeTestRule.setContent { MainPlaceHolder(mockNavigationActions) }
  }

  @Test
  fun mainPlaceHolderIsDisplayed() = run {
    onComposeScreen<MainPlaceHolder>(composeTestRule) {
      assertIsDisplayed()
      profileButton.assertIsDisplayed()
    }
  }

  @Test
  fun canNavigateToProfileScreen() = run {
    onComposeScreen<MainPlaceHolder>(composeTestRule) {
      profileButton.performClick()
      verify { mockNavigationActions.navigateTo(Route.PROFILE) }
    }
  }
}
