package ch.epfl.cs311.wanderwave.model

import android.net.Uri
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DataClassesTest {
  // Testing of all the data classes, I think it's better to test them all together
  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var trackConnection: TrackConnection
  private lateinit var profileConnection: ProfileConnection

  @RelaxedMockK private lateinit var document: DocumentSnapshot

  @Before
  fun setup() {
    MockKAnnotations.init(this)

    trackConnection = mockk<TrackConnection>(relaxed = true)
    profileConnection = mockk<ProfileConnection>(relaxed = true)

    // Set up the document mock to return some tracks
    every { document.id } returns "someId"
    every { document["title"] } returns "someTitle"
    every { document["artist"] } returns "someArtist"
    // Set up the documentTrack mock to return some beacons
    every { document.exists() } returns true
    every { document.get("location") } returns
        mapOf("latitude" to 1.0, "longitude" to 1.0, "name" to "Test Location")
    every { document.getString("firstName") } returns "TestFirstName"
    every { document.getString("lastName") } returns "TestLastName"
    every { document.getString("description") } returns "TestDescription"
    every { document.getLong("numberOfLikes") } returns 10L
    every { document.getBoolean("isPublic") } returns true
    every { document.getString("profilePictureUri") } returns "https://example.com/profile.jpg"
    every { document.getString("spotifyUid") } returns "TestSpotifyUid"
    every { document.getString("firebaseUid") } returns "TestFirebaseUid"
  }

  @Test
  fun documentSnapshotToProfile() {
    every { document.exists() } returns true
    val profile = Profile.from(document)
    // assert if the profile is not null
    assert(profile != null)

    every { document.exists() } returns false

    val profile2 = Profile.from(document)

    // assert if the profile is null
    assert(profile2 == null)
  }

  @Test
  fun documentToProfileExistWithNullValues() {
    every { document.exists() } returns true
    every { document.getString("firstName") } returns null
    every { document.getString("lastName") } returns null
    every { document.getString("description") } returns null
    every { document.getLong("numberOfLikes") } returns null
    every { document.getBoolean("isPublic") } returns null
    every { document.getString("profilePictureUri") } returns null
    every { document.getString("spotifyUid") } returns null
    every { document.getString("firebaseUid") } returns null

    val profile = Profile.from(document)
    // assert if the profile is not null
    assert(profile != null)
    // assert if the profile has the default values
    assertEquals("", profile!!.firstName)
    assertEquals("", profile.lastName)
    assertEquals("", profile.description)
    assertEquals(0, profile.numberOfLikes)
    assertEquals(false, profile.isPublic)
    assertEquals(null, profile.profilePictureUri)
    assertEquals("", profile.spotifyUid)
    assertEquals("", profile.firebaseUid)
  }

  @Test
  fun toLocationReturnsCorrectLocation() {
    val location = Location(45.0, 7.0, "Test Location")
    val result = location.toLocation()
    assertEquals(location, result)
  }

  @Test
  fun toLocationReturnsNewInstance() {
    val location = Location(45.0, 7.0, "Test Location")
    val result = location.toLocation()
    assertNotSame(location, result)
  }

  @Test
  fun documentSnapshotToBeacon() {
    every { document.exists() } returns true

    val beacon = Beacon.from(document)
    // assert if the beacon is not null
    assert(beacon != null)

    every { document.exists() } returns false

    val beacon2 = Beacon.from(document)

    // assert if the beacon is null
    assert(beacon2 == null)
  }

  @Test
  fun documentToBeaconExistWithNullValues() {
    every { document.exists() } returns true
    every { document.get("location") } returns null

    val beacon = Beacon.from(document)
    // assert if the beacon is not null
    assert(beacon != null)
    // assert if the beacon has the default values
    assertEquals(Location(0.0, 0.0), beacon!!.location)

    every { document.get("location") } returns
        mapOf("latitude" to null, "longitude" to null, "name" to null)

    assertEquals(Location(0.0, 0.0), beacon.location)
  }

  @Test
  fun toMapTest() {
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "Test description",
            numberOfLikes = 10,
            isPublic = true,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "spotify123",
            firebaseUid = "firebase123")

    val expectedMap =
        hashMapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "description" to "Test description",
            "numberOfLikes" to 10,
            "spotifyUid" to "spotify123",
            "firebaseUid" to "firebase123",
            "isPublic" to true,
            "profilePictureUri" to "https://example.com/image.jpg",
            "chosenSongs" to listOf<DocumentReference>(),
            "topSongs" to listOf<DocumentReference>(),
            "bannedSongs" to listOf<DocumentReference>())

    val mockFirebaseFirestore = mockk<FirebaseFirestore>()
    assertEquals(expectedMap, profile.toMap(mockFirebaseFirestore))
  }

  @Test
  fun basicVariablesLocation() {
    val location = Location(1.0, 1.0, "Test Location")
    val latLng: LatLng = location.toLatLng()
    val locationMap: Map<String, Any> = location.toMap()
    // test location behaviour
    assertEquals(1.0, location.latitude)
    assertEquals(1.0, location.longitude)
    assertEquals("Test Location", location.name)
    assertEquals(1.0, latLng.latitude)
    assertEquals(1.0, latLng.longitude)
    assertEquals(1.0, locationMap["latitude"])
    assertEquals(1.0, locationMap["longitude"])
    assertEquals("Test Location", locationMap["name"])
  }

  @Test
  fun DocumentSnapshotToTrack() {
    every { document.exists() } returns true
    val track = Track.from(document)
    // assert if the track is not null
    assert(track != null)

    every { document.exists() } returns false

    val track2 = Track.from(document)

    // assert if the track is null
    assert(track2 == null)
  }

  @Test
  fun profileTrackAssociationInitializesCorrectly() {
    val mockProfile = mockk<Profile>()
    val mockTrack = mockk<Track>()
    val association = ProfileTrackAssociation(mockProfile, mockTrack)

    assertEquals(mockProfile, association.profile)
    assertEquals(mockTrack, association.track)
  }

  @Test
  fun profileTrackToMapTest() {
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "Test description",
            numberOfLikes = 10,
            isPublic = true,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "spotify123",
            firebaseUid = "firebase123")

    val track = Track() // Assuming Track has a no-argument constructor

    val profileTrackAssociation = ProfileTrackAssociation(profile, track)

    val mockFirebaseFirestore = mockk<FirebaseFirestore>()
    val mockDocumentReference = mockk<DocumentReference>()

    every { mockFirebaseFirestore.collection(any()) } returns
        mockk { every { document(any()) } returns mockDocumentReference }

    val expectedMap =
        hashMapOf(
            "creator" to
                profile.firebaseUid.let { mockFirebaseFirestore.collection("users").document(it) },
            "track" to mockFirebaseFirestore.collection("tracks").document(track.id),
            "likersId" to emptyList<String>(),
            "likes" to 0)

    assertEquals(expectedMap, profileTrackAssociation.toMap(mockFirebaseFirestore))

    // test if the profile is null
    val profileTrackAssociation2 = ProfileTrackAssociation(null, track)
    val expectedMap2 =
        hashMapOf(
            "creator" to null,
            "track" to mockFirebaseFirestore.collection("tracks").document(track.id),
            "likersId" to emptyList<String>(),
            "likes" to 0)
    assertEquals(expectedMap2, profileTrackAssociation2.toMap(mockFirebaseFirestore))
  }

  @Test
  fun testProfileTrackLikeTrack() {
    val mockProfile =
        Profile("firstname", "lastname", "description", 0, true, null, "spotifyUid", "firebaseUid")
    val mockTrack = mockk<Track>()
    val association = ProfileTrackAssociation(mockProfile, mockTrack)

    val likedAssociation = association.likeTrack(mockProfile)

    assertEquals(1, likedAssociation.likes)
    assertEquals(1, likedAssociation.likersId.size)
    assertEquals(mockProfile.firebaseUid, likedAssociation.likersId[0])
  }

  @Test
  fun testProfileTrackUnlikeTrack() {
    val mockProfile =
        Profile("firstname", "lastname", "description", 0, true, null, "spotifyUid", "firebaseUid")
    val mockTrack = mockk<Track>()
    val association = ProfileTrackAssociation(mockProfile, mockTrack)

    val likedAssociation = association.likeTrack(mockProfile)
    val unlikedAssociation = likedAssociation.unlikeTrack(mockProfile)

    assertEquals(0, unlikedAssociation.likes)
    assertEquals(0, unlikedAssociation.likersId.size)
  }

  @Test
  fun noArgumentConstructorCreatesEmptyTrack() {
    val track = Track()
    assertEquals("", track.id)
    assertEquals("", track.title)
    assertEquals("", track.artist)
  }

  @Test
  fun documentToTrackExistWithNullValues() {
    every { document.exists() } returns true
    every { document.id } returns "someId"
    every { document.getString("title") } returns null
    every { document.getString("artist") } returns null

    val track = Track.from(document)
    // assert if the track is not null
    assert(track != null)
    // assert if the track has the default values
    assertEquals("someId", track!!.id)
    assertEquals("", track.title)
    assertEquals("", track.artist)
  }

  @Test
  fun distanceBetween_returnsZeroForSameLocation() {
    val location = Location(46.519962, 6.633597)
    assertEquals(0.0, location.distanceBetween(location), 0.001)
  }

  @Test
  fun distanceBetween_returnsCorrectDistanceForDifferentLocations() {
    val location1 = Location(46.803246, 7.139212)
    val location2 = Location(46.029423, 8.835748)
    val expectedDistance = 156.0
    assertEquals(expectedDistance, location1.distanceBetween(location2), 1.0)
  }

  fun profileTrackAssociation_equalsReturnsTrueForSameData() {
    val mockProfile = mockk<Profile>()
    val mockTrack = mockk<Track>()
    val association1 = ProfileTrackAssociation(mockProfile, mockTrack)
    val association2 = ProfileTrackAssociation(mockProfile, mockTrack)

    assertEquals(association1, association2)
  }

  @Test
  fun profileTrackAssociation_equalsReturnsFalseForDifferentData() {
    val mockProfile1 = mockk<Profile>()
    val mockProfile2 = mockk<Profile>()
    val mockTrack = mockk<Track>()
    val association1 = ProfileTrackAssociation(mockProfile1, mockTrack)
    val association2 = ProfileTrackAssociation(mockProfile2, mockTrack)

    assertNotEquals(association1, association2)
  }

  @Test
  fun profileTrackAssociation_hashCodeIsConsistent() {
    val mockProfile = mockk<Profile>()
    val mockTrack = mockk<Track>()
    val association = ProfileTrackAssociation(mockProfile, mockTrack)

    val initialHashCode = association.hashCode()
    assertEquals(initialHashCode, association.hashCode())
  }

  //  Beacon.kt
  //  fun updateProfileAndTrackElement(newTrackProfile: ProfileTrackAssociation): Beacon {
  //    val newProfileAndTrack = profileAndTrack.toMutableList()
  //    newProfileAndTrack.removeIf { it.track.id == newTrackProfile.track.id }
  //    newProfileAndTrack.add(newTrackProfile)
  //    return Beacon(id, location, newProfileAndTrack)
  //  }
  @Test
  fun testUpdateProfileAndTrackElement() {
    val mockProfile1 = mockk<Profile>()
    val mockProfile2 = mockk<Profile>()
    val mockTrack1 = mockk<Track>()
    val mockTrack2 = mockk<Track>()
    val mockTrack3 = mockk<Track>()
    val mockTrack4 = mockk<Track>()
    every { mockTrack1.id } returns "someId1"
    every { mockTrack2.id } returns "someId2"
    every { mockTrack3.id } returns "someId3"
    every { mockTrack4.id } returns "someId4"
    val mockProfileTrackAssociation1 = ProfileTrackAssociation(mockProfile1, mockTrack1)
    val mockProfileTrackAssociation2 = ProfileTrackAssociation(mockProfile2, mockTrack2)
    val mockProfileTrackAssociation3 = ProfileTrackAssociation(mockProfile1, mockTrack3)
    val mockProfileTrackAssociation4 = ProfileTrackAssociation(mockProfile2, mockTrack4)
    val beacon =
        Beacon(
            "id",
            Location(0.0, 0.0),
            mutableListOf(mockProfileTrackAssociation1, mockProfileTrackAssociation2))
    val newBeacon = beacon.updateProfileAndTrackElement(mockProfileTrackAssociation3)
    assertEquals(3, newBeacon.profileAndTrack.size)
    assertEquals(mockProfileTrackAssociation1, newBeacon.profileAndTrack[0])
    assertEquals(mockProfileTrackAssociation2, newBeacon.profileAndTrack[1])
    val newBeacon2 = newBeacon.updateProfileAndTrackElement(mockProfileTrackAssociation4)
    assertEquals(4, newBeacon2.profileAndTrack.size)
  }

  @Test
  fun testProfileTrackToMap() {
    val mockProfile = mockk<Profile>()
    val mockTrack = mockk<Track>()
    val mockFirebaseFirestore = mockk<FirebaseFirestore>()
    val mockDocumentReference = mockk<DocumentReference>()
    every { mockProfile.firebaseUid } returns "firebaseUid"
    every { mockTrack.id } returns "trackId"
    every { mockFirebaseFirestore.collection("users").document("firebaseUid") } returns
        mockDocumentReference
    every { mockFirebaseFirestore.collection("tracks").document("trackId") } returns
        mockDocumentReference
    val profileTrackAssociation = ProfileTrackAssociation(mockProfile, mockTrack)
    val expectedMap =
        hashMapOf(
            "creator" to mockDocumentReference,
            "track" to mockDocumentReference,
            "likersId" to emptyList<String>(),
            "likes" to 0)
    assertEquals(expectedMap, profileTrackAssociation.toMap(mockFirebaseFirestore))
  }

  @Test
  fun testProfileTrackIsLiked() = run {
    val mockProfile = mockk<Profile>()
    every { mockProfile.firebaseUid } returns "firebaseUid"
    every { mockProfile.numberOfLikes } returns 0
    val mockTrack = mockk<Track>()
    val profileTrackAssociation = ProfileTrackAssociation(mockProfile, mockTrack)
    every { mockTrack.id } returns "trackId"
    every { mockProfile.numberOfLikes = 1 } just Runs
    val likedAssociation = profileTrackAssociation.likeTrack(mockProfile)
    assert(likedAssociation.isLiked(mockProfile))
  }

  @Test
  fun testProfileTrackLikeTrack2() = run {
    val mockProfile = mockk<Profile>(relaxed = true)
    every { mockProfile.numberOfLikes = 1 } just Runs
    every { mockProfile.firebaseUid } returns "firebaseUid"
    val mockTrack = mockk<Track>()
    val profileTrackAssociation = ProfileTrackAssociation(mockProfile, mockTrack)
    every { mockTrack.id } returns "trackId"
    val likedAssociation = profileTrackAssociation.likeTrack(mockProfile)
    assertEquals(1, likedAssociation.likes)
    assertEquals(1, likedAssociation.likersId.size)
    assertEquals("firebaseUid", likedAssociation.likersId[0])
  }

  @Test
  fun testProfileTrackUnlikeTrack2() = run {
    val mockProfile = mockk<Profile>(relaxed = true)
    every { mockProfile.numberOfLikes = 0 } just Runs
    every { mockProfile.firebaseUid } returns "firebaseUid"
    val mockTrack = mockk<Track>()
    val profileTrackAssociation = ProfileTrackAssociation(mockProfile, mockTrack)
    every { mockTrack.id } returns "trackId"
    val unlikedAssociation = profileTrackAssociation.unlikeTrack(mockProfile)
    assertEquals(0, unlikedAssociation.likes)
    assertEquals(0, unlikedAssociation.likersId.size)
  }

  @Test
  fun testProfileTrackFrom() = run {
    val mockProfile = mockk<Profile>()
    val mockTrack = mockk<Track>()
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()
    every { mockDocumentSnapshot.exists() } returns true
    every { mockDocumentSnapshot.getString("firstName") } returns "firstName"
    every { mockDocumentSnapshot.getString("lastName") } returns "lastName"
    every { mockDocumentSnapshot.getString("description") } returns "description"
    every { mockDocumentSnapshot.getLong("numberOfLikes") } returns 0
    every { mockDocumentSnapshot.getBoolean("isPublic") } returns true
    every { mockDocumentSnapshot.getString("profilePictureUri") } returns "profilePictureUri"
    every { mockDocumentSnapshot.getString("spotifyUid") } returns "spotifyUid"
    every { mockDocumentSnapshot.getString("firebaseUid") } returns "firebaseUid"
    every { mockDocumentSnapshot["likersId"] } returns emptyList<String>()
    every { mockDocumentSnapshot["likes"] } returns 0
    every { mockDocumentSnapshot.id } returns "mockDocumentSnapshotID"
    every { mockDocumentSnapshot["title"] } returns "mockDocumentSnapshotID"
    every { mockDocumentSnapshot.getString("title") } returns "someTitle"
    every { mockDocumentSnapshot.getString("artist") } returns "someTitle"
    every { mockDocumentSnapshot.exists() } returns true
    val profileTrackAssociation =
        ProfileTrackAssociation.from(mapOf(), mockDocumentSnapshot, mockDocumentSnapshot)
    assert(profileTrackAssociation != null)
  }

  @Test
  fun testProfileTrackFromNull() = run {
    val mockProfile = mockk<Profile>()
    val mockTrack = mockk<Track>()
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()
    every { mockDocumentSnapshot.exists() } returns false
    val profileTrackAssociation =
        ProfileTrackAssociation.from(mapOf(), mockDocumentSnapshot, mockDocumentSnapshot)
    assert(profileTrackAssociation == null)
  }
}
