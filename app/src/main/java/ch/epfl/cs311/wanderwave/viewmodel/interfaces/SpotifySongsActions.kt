package ch.epfl.cs311.wanderwave.viewmodel.interfaces

import ch.epfl.cs311.wanderwave.model.data.Track
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.flow.StateFlow

interface SpotifySongsActions {

  /** The list of liked songs of the user. */
  val likedSongsTrackList: StateFlow<List<ListItem>>

  /** The number of liked songs of the user. */
  val nbrLikedSongs: StateFlow<Int>

  /**
   * Add a track to the list of the user's list. The list is specified by the listName parameter.
   *
   * @param listName The list to add the track to.
   * @param track The track to add to the list.
   */
  fun addTrackToList(track: Track)

  /** Get all the liked tracks of the user and add them to the likedSongs list. */
  suspend fun getLikedTracks(page: Int = 0)

  /** Get the total number of liked tracks of the user. */
  suspend fun getTotalLikedTracks()

  /** Clear the liked songs list. */
  fun clearLikedSongs()
}
