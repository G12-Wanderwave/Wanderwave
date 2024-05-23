import android.util.Log
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationUserData
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.data.TrackRecord
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import com.spotify.protocol.types.ListItem
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackListViewModelTest {

  private lateinit var viewModel: TrackListViewModel

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var repository: TrackRepository
  @RelaxedMockK private lateinit var appDatabase: AppDatabase
  @RelaxedMockK private lateinit var mockAuthenticationController: AuthenticationController
  @RelaxedMockK private lateinit var mockProfileRepository: ProfileRepository

  private val testDispatcher = TestCoroutineDispatcher()
  private lateinit var track: Track

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    val connectResult = SpotifyController.ConnectResult.SUCCESS
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)

    repository = mockk()

    track = Track("spotify:track:1cNf5WAYWuQwGoJyfsHcEF", "Across The Stars", "John Williams")

    val track1 = Track("spotify:track:6ImuyUQYhJKEKFtlrstHCD", "Main Title", "John Williams")
    val track2 =
        Track("spotify:track:0HLQFjnwq0FHpNVxormx60", "The Nightingale", "Percival Schuttenbach")
    val track3 =
        Track("spotify:track:2NZhNbfb1rD1aRj3hZaoqk", "The Imperial Suite", "Michael Giacchino")
    val track4 = Track("spotify:track:5EWPGh7jbTNO2wakv8LjUI", "Free Bird", "Lynyrd Skynyrd")
    val track5 = Track("spotify:track:4rTlPsga6T8yiHGOvZAPhJ", "Godzilla", "Eminem")

    val trackA = Track("spotify:track:5PbMSJZcNA3p2LZv7C56cm", "Yeah", "Queen")
    val trackB = Track("spotify:track:3C7RbG9Co0zjO7CsuEOqRa", "Sing for the Moment", "Eminem")

    val trackList =
        listOf(
            trackA,
            trackB,
            track,
            track1,
            track2,
            track3,
            track4,
            track5,
        )

    every { repository.getAll() } returns flowOf(trackList)

    every { mockAuthenticationController.isSignedIn() } returns true
    every { mockAuthenticationController.getUserData() } returns
        AuthenticationUserData("uid", "email", "name", "http://photoUrl/img.jpg")

    val bannedSongs = listOf(track5)
    val mockProfile = mockk<Profile>()
    every { mockProfileRepository.getItem(any()) } returns flowOf(Result.success(mockProfile))
    every { mockProfile.bannedSongs } returns bannedSongs

    viewModel =
        TrackListViewModel(
            mockSpotifyController,
            appDatabase,
            repository,
            mockProfileRepository,
            mockAuthenticationController)

    runBlocking { viewModel.uiState.first { !it.loading } }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  fun collapseTrackList() = run {
    viewModel.collapse()
    assertFalse(viewModel.uiState.value.expanded)
  }

  @Test
  fun expandTrackList() = run {
    viewModel.expand()
    assertTrue(viewModel.uiState.value.expanded)
  }

  @Test
  fun playTrack() = run {
    viewModel.playTrack(track)
    verify { mockSpotifyController.playTrackList(any(), track) }
  }

  @Test
  fun testRetrieveSubsectionAndChildrenFlow() =
      testDispatcher.runBlockingTest {
        val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
        every { mockSpotifyController.getAllElementFromSpotify() } returns
            flowOf(listOf(expectedListItem))
        every {
          mockSpotifyController.getAllChildren(
              ListItem("id", "title", null, "subtitle", "", false, true))
        } returns flowOf(listOf(expectedListItem))

        viewModel.retrieveAndAddSubsection()
        viewModel.retrieveChild(expectedListItem)

        advanceUntilIdle() // This replaces advanceTimeBy and ensures all coroutines are completed

        val result = viewModel.spotifySubsectionList.first() // Safely access the first item
        val result2 = viewModel.childrenPlaylistTrackList.first() // Safely access the first item
        Log.d(
            "TrackListViewModel23",
            "retrieveAndAddSubsection: ${viewModel.spotifySubsectionList.value}")

        assertEquals(listOf(expectedListItem), result)
        assertEquals(listOf(expectedListItem), result2)
      }

  @Test
  fun testAddTrackToList() = runBlocking {
    val track = Track("spotify:track:1cNf5WAYWuQwGoJyfsHcEF", "Across The Stars", "John Williams")
    viewModel.addTrackToList(ListType.TOP_SONGS, track)
    assertTrue(viewModel.uiState.value.tracks.contains(track))
  }

  @Test
  fun testAddTrackToListWithoutPrefix() = runBlocking {
    val track = Track("spotify:track:1cNf5WAYWuQwGoJyfsHcEF", "Across The Stars", "John Williams")
    val trackWithoutPrefix = Track("1cNf5WAYWuQwGoJyfsHcEF", "Across The Stars", "John Williams")
    viewModel.addTrackToList(ListType.TOP_SONGS, trackWithoutPrefix)
    assertTrue(viewModel.uiState.value.tracks.contains(track))
  }

  @Test
  fun testLoadRecentlyAddedTracks() = runBlockingTest {
    // Arrange
    val testTrackRecord = TrackRecord(0, "testTitle", "testArtist", 0.1.toLong())
    val testTrack = Track("testId", "testTitle", "testArtist")
    val testTrackRecords = listOf(testTrackRecord)
    val testTrackDetails = listOf(testTrack)

    every { appDatabase.trackRecordDao().getAllRecentlyAddedTracks() } returns
        flowOf(testTrackRecords)
    every { repository.getItem(testTrackRecord.trackId) } returns flowOf(Result.success(testTrack))

    // Act
    viewModel.loadRecentlyAddedTracks()

    // Assert
    assertEquals(testTrackDetails, viewModel.uiState.value.tracks)
    assertEquals(false, viewModel.uiState.value.loading)

    // null case of repository
    every { repository.getItem(testTrackRecord.trackId) } returns
        flowOf(Result.failure(Exception()))
    viewModel.loadRecentlyAddedTracks()

    // Assert
    assertEquals(emptyList<Track>(), viewModel.uiState.value.tracks)
    assertEquals(false, viewModel.uiState.value.loading)
  }

  @Test
  fun emptyChildrenList_clearsChildrenPlaylistTrackList() = runBlockingTest {

    // Act
    viewModel.emptyChildrenList()
  }

  @Test
  fun bannedTracksAreInUiState() = runBlocking {
    viewModel.updateBannedSongs()
    assertTrue(
        viewModel.uiState.value.bannedTracks.toString(),
        viewModel.uiState.value.bannedTracks.size == 1)
  }
}
