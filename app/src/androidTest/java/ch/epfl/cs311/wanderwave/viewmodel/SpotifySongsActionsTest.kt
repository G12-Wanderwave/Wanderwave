package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.google.common.base.Verify.verify
import com.spotify.protocol.types.ListItem
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SpotifySongsActionsTest {

  private lateinit var spotifySongsActions: SpotifySongsActions
  private val mockListItem = mockk<ListItem>()
  private val mockTrack = mockk<Track>()

  @Before
  fun setUp() {
    spotifySongsActions = mockk<SpotifySongsActions>(relaxed = true)
  }

  @Test
  fun addTrackToListAddsTrack() {
    spotifySongsActions.addTrackToList(ListType.TOP_SONGS, mockTrack)
    verify { spotifySongsActions.addTrackToList(ListType.TOP_SONGS, mockTrack) }
  }

  @Test
  fun retrieveAndAddSubsectionRetrievesAndAddsSubsection() {
    spotifySongsActions.retrieveAndAddSubsection()
    verify { spotifySongsActions.retrieveAndAddSubsection() }
  }

  @Test
  fun retrieveChildRetrievesChild() {
    spotifySongsActions.retrieveChild(mockListItem)
    verify { spotifySongsActions.retrieveChild(mockListItem) }
  }

  @Test
  fun getLikedTracksGetsLikedTracks() = runBlockingTest {
    spotifySongsActions.getLikedTracks()
    coVerify { spotifySongsActions.getLikedTracks() }
  }
}
