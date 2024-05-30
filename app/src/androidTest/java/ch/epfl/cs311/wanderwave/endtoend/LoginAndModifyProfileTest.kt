package ch.epfl.cs311.wanderwave.endtoend

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.MainActivity
import ch.epfl.cs311.wanderwave.di.RepositoryModule
import ch.epfl.cs311.wanderwave.di.ServiceModule
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.RecentlyPlayedRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.ui.screens.AppBottomBarScreen
import ch.epfl.cs311.wanderwave.ui.screens.EditProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import ch.epfl.cs311.wanderwave.viewmodel.SpotifyConnectScreenViewModel
import com.google.android.gms.maps.LocationSource
import com.google.firebase.auth.FirebaseAuth
import com.spotify.protocol.types.ListItem
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
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@UninstallModules(RepositoryModule::class, ServiceModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginAndModifyProfileTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule val hiltRule = HiltAndroidRule(this)

  val dispatcher = UnconfinedTestDispatcher()

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @RelaxedMockK private lateinit var mockProfileViewModel: ProfileViewModel

  @RelaxedMockK public lateinit var mockAuthenticationController: AuthenticationController

  @RelaxedMockK private lateinit var mockSpotifyViewModel: SpotifyConnectScreenViewModel
  @RelaxedMockK private lateinit var profileRepository: ProfileConnection

  @After
  fun clearMocks() {
    clearAllMocks() // Clear all MockK mocks
  }

  private fun setup(uiState: SpotifyConnectScreenViewModel.UiState) {
    mockDependencies()
    every { mockSpotifyViewModel.uiState } returns MutableStateFlow(uiState)
    mockProfileViewModel =
        ProfileViewModel(
            profileRepository,
            spotifyController,
            authenticationController = mockAuthenticationController)
  }

  private fun mockDependencies() {
    // Mocking ProfileRepositoryImpl
    // Mocking SpotifyController
    coEvery { spotifyController.getChildren(any()) } returns
        flowOf(ListItem("", "", null, "", "", false, false))
    coEvery { spotifyController.getAllElementFromSpotify() } returns
        flowOf(listOf(ListItem("", "", null, "", "", false, false)))
    coEvery { spotifyController.getAllChildren(any()) } returns
        flowOf(listOf(ListItem("", "", null, "", "", false, false)))
  }

  @Test
  fun endToEndLoginAddSong() = runBlockingTest {
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
      every { spotifyController.isConnected() } returns false
      every { spotifyController.connectRemote() } returns
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
    onComposeScreen<ProfileScreen>(composeTestRule) {
      assertIsDisplayed()
      profileSwitch.assertIsDisplayed()
      clickableIcon.assertIsDisplayed()
      outputFirstName {
        assertIsDisplayed()
        assertTextContains("My FirstName")
      }
      outputDescription {
        assertIsDisplayed()
        assertTextContains("My Description")
      }
      outputLastName {
        assertIsDisplayed()
        assertTextContains("My LastName")
      }
      clickableIcon.performClick()
    }

    onComposeScreen<EditProfileScreen>(composeTestRule) {
      assertIsDisplayed()
      inputFirstName {
        performTextClearance()
        assertIsDisplayed()
        performTextInput("Declan")
        assertTextContains("Declan")
      }
      inputLastName {
        performTextClearance()
        assertIsDisplayed()
        performTextInput("Rice")
        assertTextContains("Rice")
      }
      inputDescription {
        performTextClearance()
        assertIsDisplayed()
        performTextInput("KDOT is back <3")
        assertTextContains("KDOT is back <3")
      }
      cancelButton { assertIsDisplayed() }
      saveButton {
        assertIsDisplayed()
        performClick()
      }
    }

    onComposeScreen<ProfileScreen>(composeTestRule) {
      assertIsDisplayed()
      profileSwitch.assertIsDisplayed()
      clickableIcon.assertIsDisplayed()
      outputFirstName {
        assertIsDisplayed()
        assertTextContains("Declan")
      }
      outputDescription {
        assertIsDisplayed()
        assertTextContains("KDOT is back <3")
      }
      outputLastName {
        assertIsDisplayed()
        assertTextContains("Rice")
      }
      clickableIcon.performClick()
    }

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
      return repo
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
      return spotifyController
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

    @Provides
    @Singleton
    fun provideContext(): Context {
      return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideCoroutineDispatcher(): CoroutineDispatcher {
      return UnconfinedTestDispatcher()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
      return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideRecentlyPlayedRepository(): RecentlyPlayedRepository {
      return mockk(relaxed = true)
    }
  }
}
