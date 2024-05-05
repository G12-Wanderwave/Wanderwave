package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.data.Track
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface SpotifySongsActions {
    val spotifySubsectionList: StateFlow<List<ListItem>>
    val childrenPlaylistTrackList: StateFlow<List<ListItem>>

    fun addTrackToList(listName: String, track: Track)

    fun retrieveTracks()
    fun retrieveAndAddSubsection()
    fun retrieveChild(item: ListItem)
}