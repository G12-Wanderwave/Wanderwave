package ch.epfl.cs311.wanderwave.endtoend

import android.Manifest
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.MainActivity
import ch.epfl.cs311.wanderwave.di.RepositoryModule
import ch.epfl.cs311.wanderwave.di.ServiceModule
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.screens.components.ExclusivePlayerScreen
import com.google.android.gms.maps.LocationSource
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import javax.inject.Singleton
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@UninstallModules(RepositoryModule::class, ServiceModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginAndUseMediaControllerEndToEndTest :
    TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule val hiltRule = HiltAndroidRule(this)

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
      )

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController

  @RelaxedMockK private lateinit var mockAuthenticationController: AuthenticationController

  val dispatcher = UnconfinedTestDispatcher()

  private val track1 = Track("spotify:track:6ImuyUQYhJKEKFtlrstHCD", "Main Title", "John Williams")
  private val track2 =
      Track("spotify:track:0HLQFjnwq0FHpNVxormx60", "The Nightingale", "Percival Schuttenbach")
  private val track3 =
      Track("spotify:track:2NZhNbfb1rD1aRj3hZaoqk", "The Imperial Suite", "Michael Giacchino")
  private val track4 = Track("spotify:track:5EWPGh7jbTNO2wakv8LjUI", "Free Bird", "Lynyrd Skynyrd")
  private val track5 = Track("spotify:track:4rTlPsga6T8yiHGOvZAPhJ", "Godzilla", "Eminem")

  @Test
  fun endToEndLogidMediaPlayer() =
      runTest(dispatcher) {
        // Try to connect automatically

        coVerify { mockAuthenticationController.refreshTokenIfNecessary() }

        // Click login button on login screen

        coEvery { mockAuthenticationController.authenticate(any()) } returns flowOf(true)

        Intents.init()
        val responseDummyIntent = Intent("responseDummy")
        val result = Instrumentation.ActivityResult(123, responseDummyIntent)
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result)

        mockkStatic(AuthorizationClient::class)
        every { AuthorizationClient.createLoginActivityIntent(any(), any()) } returns
            mockk(relaxed = true)

        // Mock the response of the login
        val response = mockk<AuthorizationResponse>(relaxed = true)
        every { response.type } returns AuthorizationResponse.Type.CODE
        every { AuthorizationClient.getResponse(any(), any()) } returns response

        ComposeScreen.onComposeScreen<LoginScreen>(composeTestRule) {
          assertIsDisplayed()

          // After login, the connect should work
          coEvery { mockAuthenticationController.refreshTokenIfNecessary() } returns true
          coEvery { mockAuthenticationController.isSignedIn() } returns true
          every { mockSpotifyController.isConnected() } returns false
          every { mockSpotifyController.connectRemote() } returns
              flowOf(SpotifyController.ConnectResult.SUCCESS)

          signInButton.performClick()

          Intents.intended(IntentMatchers.anyIntent())
          Intents.assertNoUnverifiedIntents()
        }

        ComposeScreen.onComposeScreen<AppScreen>(composeTestRule) {
          assertIsDisplayed()
          appScaffold.assertIsDisplayed()
          appBottomBar.assertIsDisplayed()

          trackListButton.assertIsDisplayed()
          trackListButton.performClick()
        }

        ComposeScreen.onComposeScreen<TrackListScreen>(composeTestRule) {
          assertIsDisplayed()
          trackButton.assertIsDisplayed()
          searchBar.assertIsDisplayed()

          trackButton.performClick()
        }

        ComposeScreen.onComposeScreen<AppScreen>(composeTestRule) {
          miniPlayer.assertIsDisplayed()

          miniPlayerPlayButton.assertIsDisplayed()
          miniPlayerPlayButton.performClick()

          miniPlayerTitle.assertIsDisplayed()
          miniPlayerTitle.performClick()
        }

        ComposeScreen.onComposeScreen<ExclusivePlayerScreen>(composeTestRule) {
          assertIsDisplayed()

          playerControl.assertIsDisplayed()

          trackInfo.assertIsDisplayed()
          artist.assertIsDisplayed()
          artist.assertTextContains(track1.artist)
          title.assertIsDisplayed()
          title.assertTextContains(track1.title)

          playPauseButton.assertIsDisplayed()
          playPauseButton.performClick()

          nextButton.assertIsDisplayed()
          nextButton.performClick()
          artist.assertTextContains(track2.artist)
          title.assertIsDisplayed()
          title.assertTextContains(track2.title)

          previousButton.assertIsDisplayed()
          previousButton.performClick()
          artist.assertTextContains(track1.artist)
          title.assertIsDisplayed()
          title.assertTextContains(track1.title)

          shuffleButton.assertIsDisplayed()
          shuffleButton.performClick()

          loopButton.assertIsDisplayed()
          loopButton.performClick()
          loopButton.performClick()
        }
      }

  @Module
  @InstallIn(SingletonComponent::class)
  inner class MockRepositoryModule {

    @Provides
    @Singleton
    fun provideBeaconRepository(): BeaconRepository {
      val repo = mockk<BeaconRepository>(relaxed = true)
      every { repo.getAll() } returns flowOf(emptyList())
      return repo
    }

    @Provides
    @Singleton
    fun provideTrackRepository(): TrackRepository {
      val repo = mockk<TrackRepository>(relaxed = true)

      val trackList =
          listOf(
              track1,
              track2,
              track3,
              track4,
              track5,
          )

      every { repo.getAll() } returns flowOf(trackList)
      return repo
    }

    @Provides
    @Singleton
    fun provideProfileRepository(): ProfileRepository {
      val repo = mockk<ProfileRepository>(relaxed = true)
      return repo
    }

    @Provides
    @Singleton
    fun provideAuthTokenRepository(): AuthTokenRepository {
      val repo = mockk<AuthTokenRepository>(relaxed = true)
      coEvery { repo.getAuthToken(any()) } returns "mockedToken"
      return mockk(relaxed = true)
    }
  }

  @Module
  @InstallIn(SingletonComponent::class)
  inner class MockServiceModule {

    @Provides
    @Singleton
    fun provideAuthenticationController(): AuthenticationController {
      return mockAuthenticationController
    }

    @Provides
    @Singleton
    fun provideSpotifyController(): SpotifyController {
      return mockSpotifyController
    }

    @Provides
    @Singleton
    fun provideAppDatabase(): AppDatabase {
      return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideLocationSource(): LocationSource {
      return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
      return mockk(relaxed = true)
    }
  }
}
