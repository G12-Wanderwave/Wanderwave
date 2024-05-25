package ch.epfl.cs311.wanderwave.viewmodel.interfaces

import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.flow.StateFlow

interface SpotifySongsActions {

  val likedSongsTrackList: StateFlow<List<ListItem>>

  /**
   * Add a track to the list of the user's list. The list is specified by the listName parameter.
   *
   * @param listName The list to add the track to.
   * @param track The track to add to the list.
   * @author Menzo Bouaissi
   * @since 3.0
   * @last update 3.0
   */
  fun addTrackToList(track: Track)



  /**
   * Get all the liked tracks of the user and add them to the likedSongs list.
   *
   * @author Menzo Bouaissi
   * @since 3.0
   * @last update 3.0
   */
  suspend fun getLikedTracks()


}
