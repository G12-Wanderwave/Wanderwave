package ch.epfl.cs311.wanderwave.viewmodel.interfaces

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.data.ListType
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.flow.StateFlow

interface SpotifySongsActions {
    val spotifySubsectionList: StateFlow<List<ListItem>>
    val childrenPlaylistTrackList: StateFlow<List<ListItem>>

    fun addTrackToList(listName: ListType = ListType.TOP_SONGS, track: Track)

    fun retrieveTracks()
    fun retrieveAndAddSubsection()
    fun retrieveChild(item: ListItem)
}