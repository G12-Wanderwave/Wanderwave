package ch.epfl.cs311.wanderwave.viewmodel.interfaces

import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.flow.StateFlow

interface SpotifySongsActions {

  val spotifySubsectionList: StateFlow<List<ListItem>>
  val childrenPlaylistTrackList: StateFlow<List<ListItem>>
  val likedSongsTrackList: StateFlow<List<ListItem>>
  val isTopSongsListVisible: StateFlow<Boolean>

  /**
   * Add a track to the list of the user's list. The list is specified by the listName parameter.
   *
   * @param listName The list to add the track to.
   * @param track The track to add to the list.
   * @author Menzo Bouaissi
   * @since 3.0
   * @last update 3.0
   */
  fun addTrackToList(listName: ListType = ListType.TOP_SONGS, track: Track)

  /**
   * Get all the element of the main screen and add them to the spotifySubsectionList
   *
   * @author Menzo Bouaissi
   * @since 3.0
   * @last update 3.0
   */
  fun retrieveAndAddSubsection()

  /**
   * Get all the element of a subsection and add them to the childrenPlaylistTrackList
   *
   * @param item The item to retrieve the children from.
   * @author Menzo Bouaissi
   * @since 3.0
   * @last update 3.0
   */
  fun retrieveChild(item: ListItem)

  /**
   * Get all the liked tracks of the user and add them to the likedSongs list.
   *
   * @author Menzo Bouaissi
   * @since 3.0
   * @last update 3.0
   */
  suspend fun getLikedTracks()

  /**
   * Get all the tracks from a playlist
   *
   * @param playlistId The id of the playlist to get the tracks from.
   * @since 3.0
   * @last update 3.0
   */
  fun getTracksFromPlaylist(playlistId: String)
}
