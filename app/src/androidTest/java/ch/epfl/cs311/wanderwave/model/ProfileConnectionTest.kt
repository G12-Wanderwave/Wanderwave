package ch.epfl.cs311.wanderwave.model

import android.net.Uri
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

public class ProfileConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var documentSnapshot: DocumentSnapshot
  private lateinit var profileConnection: ProfileConnection

  @RelaxedMockK private lateinit var firebaseFirestore: FirebaseFirestore
  @RelaxedMockK private lateinit var querySnapshot: QuerySnapshot

  lateinit var testDispatcher: CoroutineDispatcher

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    val trackConnection = TrackConnection(firebaseFirestore, testDispatcher)
    profileConnection =
        ProfileConnection(firebaseFirestore, testDispatcher, trackConnection = trackConnection)
  }

  @Test
  fun testDocumentToItem() {
    // Mock document snapshot data
    val firstName = "John"
    val lastName = "Doe"
    val description = "Test description"
    val numberOfLikes = 5
    val isPublic = true
    val spotifyUid = "spotifyUid"
    val firebaseUid = "firebaseUid"

    // Mock the behavior of Document
    every { documentSnapshot.exists() } returns true
    every { documentSnapshot.getString("firstName") } returns firstName
    every { documentSnapshot.getString("lastName") } returns lastName
    every { documentSnapshot.getString("description") } returns description
    every { documentSnapshot.getLong("numberOfLikes") } returns numberOfLikes.toLong()
    every { documentSnapshot.getBoolean("isPublic") } returns isPublic
    every { documentSnapshot.getString("spotifyUid") } returns spotifyUid
    every { documentSnapshot.getString("firebaseUid") } returns firebaseUid
    every { documentSnapshot.getString("profilePictureUri") } returns null

    // Call the function under test
    val result = profileConnection.documentToItem(documentSnapshot)

    // Assert the result
    val expectedProfile =
        Profile(
            firstName = firstName,
            lastName = lastName,
            description = description,
            numberOfLikes = numberOfLikes.toInt(),
            isPublic = isPublic,
            profilePictureUri = null,
            spotifyUid = spotifyUid,
            firebaseUid = firebaseUid)
    assertEquals(expectedProfile, result)
  }

  @Test
  fun itemToMapTest() {
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

    val expectedMap: HashMap<String, Any> =
        hashMapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "description" to "Test description",
            "numberOfLikes" to 10,
            "spotifyUid" to "spotify123",
            "firebaseUid" to "firebase123",
            "isPublic" to true,
            "profilePictureUri" to "https://example.com/image.jpg",
            "topSongs" to listOf<DocumentReference>(),
            "chosenSongs" to listOf<DocumentReference>(),
            "likedSongs" to listOf<DocumentReference>(),
            "bannedSongs" to listOf<DocumentReference>())

    assertEquals(expectedMap, profileConnection.itemToMap(profile))
  }

  @Test
  fun testAddItem() {
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

    val trackConnection = mockk<TrackConnection>(relaxed = true)
    val profileConnection =
        ProfileConnection(firebaseFirestore, testDispatcher, trackConnection = trackConnection)

    profileConnection.addItem(profile)

    verify { trackConnection.addItemsIfNotExist(profile.topSongs) }
    verify { trackConnection.addItemsIfNotExist(profile.chosenSongs) }
  }

  @Test
  fun testAddItemWithId() {
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

    val trackConnection = mockk<TrackConnection>(relaxed = true)
    val profileConnection =
        ProfileConnection(firebaseFirestore, testDispatcher, trackConnection = trackConnection)

    profileConnection.addItemWithId(profile)

    verify { trackConnection.addItemsIfNotExist(profile.topSongs) }
    verify { trackConnection.addItemsIfNotExist(profile.chosenSongs) }
  }

  @Test
  fun testGetItem() = runBlocking {
    withTimeout(3000) {
      // Pass the mock Firestore instance to your BeaconConnection

      val documentReference = mockk<DocumentReference>(relaxed = true)
      val collectionReference = mockk<CollectionReference>(relaxed = true)
      val trackDocumentReference = mockk<DocumentReference>(relaxed = true)
      // profileConnection = mockk<ProfileConnection>(relaxed = true)

      // make a real track
      val track =
          Track( // id, title, artist
              "Sample ID", "trackTitle", "trackArtist")

      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestProfile =
          Profile(
              "Sample First Name",
              "Sample last name",
              "Sample desc",
              0,
              false,
              Uri.parse("https://example.com/image.jpg"),
              "Sample Profile ID",
              "Sample ID",
              listOf(track),
              listOf(track, track),
              listOf(track),
              listOf(track))

      every { mockDocumentSnapshot.getData() } returns getTestProfile.toMap(firebaseFirestore)
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestProfile.firebaseUid
      every { mockDocumentSnapshot.getString("firstName") } returns getTestProfile.firstName
      every { mockDocumentSnapshot.getString("lastName") } returns getTestProfile.lastName
      every { mockDocumentSnapshot.getString("description") } returns getTestProfile.description
      every { mockDocumentSnapshot.getLong("numberOfLikes") } returns
          getTestProfile.numberOfLikes.toLong()
      every { mockDocumentSnapshot.getBoolean("isPublic") } returns getTestProfile.isPublic
      every { mockDocumentSnapshot.getString("profilePictureUri") } returns
          getTestProfile.profilePictureUri.toString()
      every { mockDocumentSnapshot.getString("spotifyUid") } returns getTestProfile.spotifyUid
      every { mockDocumentSnapshot.getString("firebaseUid") } returns getTestProfile.firebaseUid
      every { mockDocumentSnapshot.get("topSongs") } returns
          getTestProfile.topSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("chosenSongs") } returns
          getTestProfile.chosenSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("bannedSongs") } returns
          getTestProfile.bannedSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("likedSongs") } returns
          getTestProfile.likedSongs.map { trackDocumentReference }

      every { mockDocumentSnapshot.getString("title") } returns track.title
      every { mockDocumentSnapshot.getString("artist") } returns track.artist

      // Define behavior for the addOnSuccessListener method
      every { documentReference.addSnapshotListener(any()) } answers
          {
            val listener = arg<EventListener<DocumentSnapshot>>(0)

            // Define the behavior of the mock DocumentSnapshot here
            listener.onEvent(mockDocumentSnapshot, null)

            mockk(relaxed = true)
          }

      every { trackDocumentReference.addSnapshotListener(any()) } answers
          {
            val listener = arg<EventListener<DocumentSnapshot>>(0)

            // Define the behavior of the mock DocumentSnapshot here
            listener.onEvent(mockDocumentSnapshot, null)

            mockk(relaxed = true)
          }

      every { firebaseFirestore.collection(profileConnection.collectionName) } returns
          collectionReference
      every { collectionReference.document("testProfile") } returns documentReference

      // Call the function under test
      val retrievedProfile =
          profileConnection
              .getItem("testProfile")
              .filter { it.getOrNull()?.topSongs?.isNotEmpty() ?: false }
              .firstOrNull()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.addSnapshotListener(any()) }

      assertEquals(Result.success<Profile>(getTestProfile), retrievedProfile)
    }
  }

  @Test
  fun testDocumentTransformNullDocument() {
    runBlocking {
      val documentSnapshot = mockk<DocumentSnapshot>()
      every { documentSnapshot.exists() } returns false

      val result = profileConnection.documentTransform(documentSnapshot, null).first()
      assert(result.isFailure)
    }
  }

  @Test
  fun testCombineFunctionWithNullAndNonNullValues() = runBlocking {
    val track1 = Track("trackId1", "Track Title 1", "Track Artist 1")
    val track2 = Track("trackId2", "Track Title 2", "Track Artist 2")
    val initialProfile =
        Profile(
            "Sample First Name",
            "Sample Last Name",
            "Sample Description",
            0,
            false,
            Uri.parse("https://example.com/image.jpg"),
            "Sample Profile ID",
            "Sample Firebase ID",
            listOf(),
            listOf(),
            listOf(),
            listOf())

    val topSongsFlow = flowOf(Result.success(listOf(track1, track2)))
    val chosenSongsFlow = flowOf(Result.success(listOf(track1, track2)))
    val bannedSongsFlow = flowOf(Result.success(listOf(track1, track2)))
    val likedSongsFlow = flowOf(Result.success(listOf(track1, track2)))

    val combinedProfileFlow =
        combine(topSongsFlow, chosenSongsFlow, bannedSongsFlow, likedSongsFlow) {
            topSongsResult,
            chosenSongsResult,
            bannedSongsResult,
            likedSongsResult ->
          initialProfile.copy(
              topSongs = topSongsResult.getOrNull() ?: initialProfile.topSongs,
              chosenSongs = chosenSongsResult.getOrNull() ?: initialProfile.chosenSongs,
              bannedSongs = bannedSongsResult.getOrNull() ?: initialProfile.bannedSongs,
              likedSongs = likedSongsResult.getOrNull() ?: initialProfile.likedSongs)
        }

    val combinedProfile = combinedProfileFlow.first()

    val expectedProfile =
        initialProfile.copy(
            topSongs = listOf(track1, track2),
            chosenSongs = listOf(track1, track2),
            bannedSongs = listOf(track1, track2),
            likedSongs = listOf(track1, track2))

    assertEquals(expectedProfile.topSongs, combinedProfile.topSongs)
    assertEquals(expectedProfile.chosenSongs, combinedProfile.chosenSongs)
    assertEquals(expectedProfile.bannedSongs, combinedProfile.bannedSongs)
    assertEquals(expectedProfile.likedSongs, combinedProfile.likedSongs)
    assertEquals(expectedProfile, combinedProfile)

    val topSongsFlowNull = flowOf<Result<List<Track>>?>(null)
    val chosenSongsFlowNull = flowOf<Result<List<Track>>?>(null)
    val bannedSongsFlowNull = flowOf<Result<List<Track>>?>(null)
    val likedSongsFlowNull = flowOf<Result<List<Track>>?>(null)

    val combinedProfileFlowNull =
        combine(topSongsFlowNull, chosenSongsFlowNull, bannedSongsFlowNull, likedSongsFlowNull) {
            topSongsResult,
            chosenSongsResult,
            bannedSongsResult,
            likedSongsResult ->
          initialProfile.copy(
              topSongs = topSongsResult?.getOrNull() ?: initialProfile.topSongs,
              chosenSongs = chosenSongsResult?.getOrNull() ?: initialProfile.chosenSongs,
              bannedSongs = bannedSongsResult?.getOrNull() ?: initialProfile.bannedSongs,
              likedSongs = likedSongsResult?.getOrNull() ?: initialProfile.likedSongs)
        }

    val combinedProfileNull = combinedProfileFlowNull.first()

    assertEquals(initialProfile.topSongs, combinedProfileNull.topSongs)
    assertEquals(initialProfile.chosenSongs, combinedProfileNull.chosenSongs)
    assertEquals(initialProfile.bannedSongs, combinedProfileNull.bannedSongs)
    assertEquals(initialProfile.likedSongs, combinedProfileNull.likedSongs)
    assertEquals(initialProfile, combinedProfileNull)

    val topSongsFlowMixed = flowOf(Result.success(listOf(track1, track2)))
    val chosenSongsFlowMixed = flowOf<Result<List<Track>>?>(null)
    val bannedSongsFlowMixed = flowOf(Result.success(listOf(track1, track2)))
    val likedSongsFlowMixed = flowOf<Result<List<Track>>?>(null)

    val combinedProfileFlowMixed =
        combine(
            topSongsFlowMixed, chosenSongsFlowMixed, bannedSongsFlowMixed, likedSongsFlowMixed) {
                topSongsResult,
                chosenSongsResult,
                bannedSongsResult,
                likedSongsResult ->
              initialProfile.copy(
                  topSongs = topSongsResult?.getOrNull() ?: initialProfile.topSongs,
                  chosenSongs = chosenSongsResult?.getOrNull() ?: initialProfile.chosenSongs,
                  bannedSongs = bannedSongsResult?.getOrNull() ?: initialProfile.bannedSongs,
                  likedSongs = likedSongsResult?.getOrNull() ?: initialProfile.likedSongs)
            }

    val combinedProfileMixed = combinedProfileFlowMixed.first()
    // ktfmt
    val expectedProfileMixed =
        initialProfile.copy(
            topSongs = listOf(track1, track2),
            chosenSongs = initialProfile.chosenSongs,
            bannedSongs = listOf(track1, track2),
            likedSongs = initialProfile.likedSongs)

    assertEquals(expectedProfileMixed.topSongs, combinedProfileMixed.topSongs)
    assertEquals(expectedProfileMixed.chosenSongs, combinedProfileMixed.chosenSongs)
    assertEquals(expectedProfileMixed.bannedSongs, combinedProfileMixed.bannedSongs)
    assertEquals(expectedProfileMixed.likedSongs, combinedProfileMixed.likedSongs)
    assertEquals(expectedProfileMixed, combinedProfileMixed)
  }

  @Test
  fun testCombine() = runBlocking {
    val initialProfile =
        Profile(
            firstName = "Sample First Name",
            lastName = "Sample Last Name",
            description = "Sample Description",
            numberOfLikes = 0,
            isPublic = false,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "Sample Profile ID",
            firebaseUid = "Sample Firebase ID",
            topSongs = listOf(),
            chosenSongs = listOf(),
            bannedSongs = listOf(),
            likedSongs = listOf())

    val track1 = Track("trackId1", "Track Title 1", "Track Artist 1")
    val track2 = Track("trackId2", "Track Title 2", "Track Artist 2")

    // Define flows with successful results
    val topSongsFlow = flowOf(Result.success(listOf(track1, track2)))
    val chosenSongsFlow = flowOf(Result.success(listOf(track1)))
    val bannedSongsFlow = flowOf(Result.success(listOf(track2)))
    val likedSongsFlow = flowOf(Result.success(listOf(track1, track2)))

    // Define flows with null results to simulate failures
    val topSongsFlowNull = flowOf<Result<List<Track>>>(Result.failure(Throwable("Error")))
    val chosenSongsFlowNull = flowOf<Result<List<Track>>>(Result.failure(Throwable("Error")))
    val bannedSongsFlowNull = flowOf<Result<List<Track>>>(Result.failure(Throwable("Error")))
    val likedSongsFlowNull = flowOf<Result<List<Track>>>(Result.failure(Throwable("Error")))

    // Combine with non-null values
    val combinedProfile =
        combine(topSongsFlow, chosenSongsFlow, bannedSongsFlow, likedSongsFlow) {
                topSongsResult,
                chosenSongsResult,
                bannedSongsResult,
                likedSongsResult ->
              initialProfile.copy(
                  topSongs = topSongsResult.getOrNull() ?: initialProfile.topSongs,
                  chosenSongs = chosenSongsResult.getOrNull() ?: initialProfile.chosenSongs,
                  bannedSongs = bannedSongsResult.getOrNull() ?: initialProfile.bannedSongs,
                  likedSongs = likedSongsResult.getOrNull() ?: initialProfile.likedSongs)
            }
            .first()

    // Check results for non-null values
    assertEquals(listOf(track1, track2), combinedProfile.topSongs)
    assertEquals(listOf(track1), combinedProfile.chosenSongs)
    assertEquals(listOf(track2), combinedProfile.bannedSongs)
    assertEquals(listOf(track1, track2), combinedProfile.likedSongs)

    // Combine with null values
    val combinedProfileWithNulls =
        combine(topSongsFlowNull, chosenSongsFlowNull, bannedSongsFlowNull, likedSongsFlowNull) {
                topSongsResult,
                chosenSongsResult,
                bannedSongsResult,
                likedSongsResult ->
              initialProfile.copy(
                  topSongs = topSongsResult.getOrNull() ?: initialProfile.topSongs,
                  chosenSongs = chosenSongsResult.getOrNull() ?: initialProfile.chosenSongs,
                  bannedSongs = bannedSongsResult.getOrNull() ?: initialProfile.bannedSongs,
                  likedSongs = likedSongsResult.getOrNull() ?: initialProfile.likedSongs)
            }
            .first()

    // Check results for null values (should fallback to initial profile values)
    assertEquals(initialProfile.topSongs, combinedProfileWithNulls.topSongs)
    assertEquals(initialProfile.chosenSongs, combinedProfileWithNulls.chosenSongs)
    assertEquals(initialProfile.bannedSongs, combinedProfileWithNulls.bannedSongs)
    assertEquals(initialProfile.likedSongs, combinedProfileWithNulls.likedSongs)
  }

  @Test
  fun testDocumentTransformStringTracks() {
    runBlocking {
      val getTestProfile =
          Profile(
              "Sample First Name",
              "Sample last name",
              "Sample desc",
              0,
              false,
              Uri.parse("https://example.com/image.jpg"),
              "Sample Profile ID",
              "Sample ID",
              listOf(),
              listOf(),
              listOf(),
              listOf())

      val mockDocumentSnapshot = mockk<DocumentSnapshot>()
      every { mockDocumentSnapshot.getData() } returns getTestProfile.toMap(firebaseFirestore)
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestProfile.firebaseUid
      every { mockDocumentSnapshot.getString("firstName") } returns getTestProfile.firstName
      every { mockDocumentSnapshot.getString("lastName") } returns getTestProfile.lastName
      every { mockDocumentSnapshot.getString("description") } returns getTestProfile.description
      every { mockDocumentSnapshot.getLong("numberOfLikes") } returns
          getTestProfile.numberOfLikes.toLong()
      every { mockDocumentSnapshot.getBoolean("isPublic") } returns getTestProfile.isPublic
      every { mockDocumentSnapshot.getString("profilePictureUri") } returns
          getTestProfile.profilePictureUri.toString()
      every { mockDocumentSnapshot.getString("spotifyUid") } returns getTestProfile.spotifyUid
      every { mockDocumentSnapshot.getString("firebaseUid") } returns getTestProfile.firebaseUid

      val trackDocumentReference = mockk<DocumentReference>(relaxed = true)
      every { mockDocumentSnapshot.get("topSongs") } returns listOf("String")
      every { mockDocumentSnapshot.get("chosenSongs") } returns
          getTestProfile.chosenSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("bannedSongs") } returns
          getTestProfile.bannedSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("likedSongs") } returns
          getTestProfile.likedSongs.map { trackDocumentReference }

      var result = profileConnection.documentTransform(mockDocumentSnapshot, null).first()
      assert(result.isSuccess)
      assertEquals(result.getOrNull(), getTestProfile)

      every { mockDocumentSnapshot.get("topSongs") } returns
          getTestProfile.topSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("chosenSongs") } returns listOf("String")
      result = profileConnection.documentTransform(mockDocumentSnapshot, null).first()
      assert(result.isSuccess)
      assertEquals(result.getOrNull(), getTestProfile)

      every { mockDocumentSnapshot.get("chosenSongs") } returns
          getTestProfile.chosenSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("bannedSongs") } returns listOf("String")
      result = profileConnection.documentTransform(mockDocumentSnapshot, null).first()
      assert(result.isSuccess)
      assertEquals(result.getOrNull(), getTestProfile)

      every { mockDocumentSnapshot.get("bannedSongs") } returns
          getTestProfile.bannedSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("likedSongs") } returns listOf("String")
      result = profileConnection.documentTransform(mockDocumentSnapshot, null).first()
      assert(result.isSuccess)
      assertEquals(result.getOrNull(), getTestProfile)
    }
  }
}
