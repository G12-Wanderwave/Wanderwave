package ch.epfl.cs311.wanderwave.endtoend

import android.app.Instrumentation
import android.content.Intent
import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.MainActivity
import ch.epfl.cs311.wanderwave.di.RepositoryModule
import ch.epfl.cs311.wanderwave.di.ServiceModule
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.ui.screens.AppBottomBarScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import com.google.android.gms.maps.LocationSource
import com.google.common.base.Verify.verify
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
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
class LoginAndLogoutTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule val hiltRule = HiltAndroidRule(this)

  @RelaxedMockK public lateinit var mockSpotifyController: SpotifyController

  val dispatcher = UnconfinedTestDispatcher()

  @RelaxedMockK public lateinit var mockAuthenticationController: AuthenticationController

  @Test
  fun endToEndLoginLogout() =
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

        val response = mockk<AuthorizationResponse>(relaxed = true)
        every { response.type } returns AuthorizationResponse.Type.CODE
        every { AuthorizationClient.getResponse(any(), any()) } returns response

        onComposeScreen<LoginScreen>(composeTestRule) {
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

          verify { mockAuthenticationController.authenticate(any()) }
        }

        // Go to profile screen

        onComposeScreen<AppBottomBarScreen>(composeTestRule) {
          assertIsDisplayed()
          bottomAppBarProfileButton.assertIsDisplayed()
          bottomAppBarProfileButton.performClick()
        }

        // Press sign out button

        onComposeScreen<ProfileScreen>(composeTestRule) {
          assertIsDisplayed()
          signOutButton.assertIsDisplayed()
          signOutButton.performClick()
        }

        // Check we're back at login

        onComposeScreen<LoginScreen>(composeTestRule) { assertIsDisplayed() }

        Log.d("LoginAndLogoutTest", "Test finished")
        Intents.release()
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
      every { repo.getAll() } returns flowOf(emptyList())
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
