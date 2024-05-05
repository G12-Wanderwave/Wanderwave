package ch.epfl.cs311.wanderwave.viewmodel.interfaces

import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.flow.StateFlow

interface SpotifySongsActions {
  /**
   * The list of subsections of the user's top songs. Each subsection is a list of tracks or
   * playlist.
   */
  val spotifySubsectionList: StateFlow<List<ListItem>>

  /**
   * The list child in the subsections of the user's top songs. Each child is a list of tracks or
   * playlist.
   */
  val childrenPlaylistTrackList: StateFlow<List<ListItem>>

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
}
