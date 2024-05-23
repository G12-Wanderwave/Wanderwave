package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationUserData
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.BeaconScreen
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class BeaconScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions
  @RelaxedMockK private lateinit var beaconConnection: BeaconConnection
  @RelaxedMockK private lateinit var trackRepository: TrackRepository
  @RelaxedMockK private lateinit var mockNavController: NavHostController
  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var mockAuthenticationController: AuthenticationController
  @RelaxedMockK private lateinit var mockProfileRepository: ProfileRepository

  @Before
  fun setup() {
    // comment
    val beaconId = "UAn8OUadgrUOKYagf8a2"

    val beaconFlow =
        flowOf(
            Result.success(
                Beacon(
                    beaconId,
                    Location(46.519653, 6.632273, "Lausanne"),
                    profileAndTrack =
                        listOf(
                            ProfileTrackAssociation(
                                Profile(
                                    "Sample First Name",
                                    "Sample last name",
                                    "Sample desc",
                                    0,
                                    false,
                                    null,
                                    "Sample Profile ID",
                                    "Sample Track ID"),
                                Track(
                                    "Sample Track ID",
                                    "Sample Track Title",
                                    "Sample Artist Name"))))))
    coEvery { beaconConnection.getItem(any<String>()) } returns beaconFlow

    val connectResult = SpotifyController.ConnectResult.SUCCESS
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)

    every { mockAuthenticationController.getUserData() } returns
        AuthenticationUserData("test-uid", "test-email", "test-name", "test-photo-url")

    val mockProfile = mockk<Profile>(relaxed = true) { every { isPublic } returns true }

    every { mockProfileRepository.getItem(any()) } returns flowOf(Result.success(mockProfile))

    val viewModel =
        BeaconViewModel(
            trackRepository,
            beaconConnection,
            mockProfileRepository,
            mockSpotifyController,
            mockAuthenticationController)

    composeTestRule.setContent { BeaconScreen(beaconId, mockNavigationActions, viewModel) }

    every { mockNavController.navigate(any<String>()) } returns Unit
    mockNavigationActions = NavigationActions(mockNavController)
  }

  @Test
  fun componentsAreDisplayed(): Unit = runBlockingTest {
    ComposeScreen.onComposeScreen<BeaconScreen>(composeTestRule) {
      assertIsDisplayed()

      beaconTitle { assertIsDisplayed() }
      beaconLocation { assertIsDisplayed() }
      beaconMap { assertIsDisplayed() }
    }
  }

  @Test
  fun canNavigateToProfileOnlyWhenProfileIsPublic() = run {
    mockNavigationActions.navigateToProfile("Sample Profile ID")
    verify { mockNavController.navigate("${Route.VIEW_PROFILE.routeString}/Sample Profile ID") }
  }
}
