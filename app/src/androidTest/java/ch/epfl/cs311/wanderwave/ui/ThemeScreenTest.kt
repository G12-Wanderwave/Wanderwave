package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ch.epfl.cs311.wanderwave.ui.screens.ThemeScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ThemeScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { ThemeScreen() }
  }

  @Test
  fun themeScreenIsDisplayed() = run {
    ComposeScreen.onComposeScreen<ThemeScreen>(composeTestRule) { assertIsDisplayed() }
  }
}
