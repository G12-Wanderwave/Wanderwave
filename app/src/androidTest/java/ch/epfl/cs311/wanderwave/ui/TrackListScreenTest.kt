package ch.epfl.cs311.wanderwave.ui
//
// import androidx.compose.ui.test.junit4.createAndroidComposeRule
// import androidx.test.ext.junit.runners.AndroidJUnit4
// import ch.epfl.cs311.wanderwave.model.data.Track
// import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
// import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
// import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
// import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
// import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
// import io.mockk.every
// import io.mockk.impl.annotations.RelaxedMockK
// import io.mockk.junit4.MockKRule
// import kotlinx.coroutines.flow.flowOf
// import org.junit.After
// import org.junit.Before
// import org.junit.Rule
// import org.junit.runner.RunWith
//
// @RunWith(AndroidJUnit4::class)
// class TrackListScreenTest : TestCase() {
//
//   @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()
//
//   @get:Rule val mockkRule = MockKRule(this)
//
//   @RelaxedMockK lateinit var mockSpotifyController: SpotifyController
//   @RelaxedMockK lateinit var mockTrackRepositoryImpl: TrackRepository
//
//   @RelaxedMockK lateinit var viewModel: TrackListViewModel
//
//   @RelaxedMockK lateinit var mockShowMessage: (String) -> Unit
//
//   @Before fun setup() {}
//
//   @After
//   fun tearDown() {
//     // Dispatchers.resetMain()
//     // comment
//   }
//
//   private fun setupViewModel(result: Boolean) {
//
//     every { mockTrackRepositoryImpl.getAll() } returns
//         flowOf(listOf(Track("id1", "title1", "artist1")))
//     every { mockSpotifyController.playTrack(any()) } returns flowOf(result)
//     viewModel = TrackListViewModel(mockSpotifyController, mockTrackRepositoryImpl)
//     composeTestRule.setContent { TrackListScreen(mockShowMessage, viewModel) }
//   }

  // @Test
  // fun trackListScreenIsDisplayed() = runTest {
  //   setupViewModel(true)
  //   onComposeScreen<TrackListScreen>(composeTestRule) {
  //     assertIsDisplayed()
  //
  //     // add text to the search bar :
  //     searchBar {
  //       assertIsDisplayed()
  //       performTextInput("search")
  //     }
  //
  //     trackButton {
  //       assertIsDisplayed()
  //       assert(hasClickAction())
  //     }
  //   }
  // }
  //
  // @Test
  // fun tappingTrackSelectssIt() = runTest {
  //   setupViewModel(true)
  //
  //   onComposeScreen<TrackListScreen>(composeTestRule) {
  //     trackButton {
  //       assertIsDisplayed()
  //       performClick()
  //       assertTrue(viewModel.uiState.value.selectedTrack != null)
  //     }
  //     advanceUntilIdle()
  //     coVerify { mockShowMessage wasNot Called }
  //   }
  // }
// }
