package ch.epfl.cs311.wanderwave.endToEnd

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.TestActivity
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.SelectSongScreen
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import ch.epfl.cs311.wanderwave.viewmodel.SpotifyConnectScreenViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class LoginAndAddSong : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockNavigationActions: NavigationActions

    @RelaxedMockK
    private lateinit var mockViewModel: SpotifyConnectScreenViewModel

    lateinit var viewModel: ProfileViewModel

    @RelaxedMockK private lateinit var profileRepositoryImpl: ProfileRepositoryImpl

    @RelaxedMockK private lateinit var spotifyController: SpotifyController



    @After
    fun clearMocks() {
        clearAllMocks() // Clear all MockK mocks
    }

    private fun setup(uiState: SpotifyConnectScreenViewModel.UiState) {
        mockDependencies()
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)
        viewModel = ProfileViewModel(profileRepositoryImpl, spotifyController)

        composeTestRule.setContent {
            SpotifyConnectScreen(navigationActions = mockNavigationActions, viewModel = mockViewModel)
            MainPlaceHolder(mockNavigationActions)
            ProfileScreen(mockNavigationActions, viewModel)
            SelectSongScreen(mockNavigationActions, viewModel)
        }

    }
    private fun mockDependencies() {
        // Mocking ProfileRepositoryImpl
        coEvery { profileRepositoryImpl.insert(any()) } just Runs
        coEvery { profileRepositoryImpl.delete() } just Runs

        // Mocking SpotifyController
        coEvery { spotifyController.getChildren(any()) } returns
                flowOf(ListItem("", "", null, "", "", false, false))
        coEvery { spotifyController.getAllElementFromSpotify() } returns
                flowOf(listOf(ListItem("", "", null, "", "", false, false)))
        coEvery { spotifyController.getAllChildren(any()) } returns
                flowOf(listOf(ListItem("", "", null, "", "", false, false)))
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loginNavigateToProfileScreenAndAddSong() = runBlockingTest() {  // Use the testDispatcher to control coroutine execution
        setup(SpotifyConnectScreenViewModel.UiState(hasResult = true, success = true))

 //       ComposeScreen.onComposeScreen<SpotifyConnectScreen>(composeTestgRule) {}

        ComposeScreen.onComposeScreen<MainPlaceHolder>(composeTestRule) {
            assertIsDisplayed()
            profileButton.assertIsDisplayed()
            profileButton.performClick()
        }

        ComposeScreen.onComposeScreen<ProfileScreen>(composeTestRule) {
            assertIsDisplayed()
            addTopSongs.assertIsDisplayed()
            addTopSongs.performClick()
        }

        ComposeScreen.onComposeScreen<SelectSongScreen>(composeTestRule) {
            assertIsDisplayed()
        }
    }

}
