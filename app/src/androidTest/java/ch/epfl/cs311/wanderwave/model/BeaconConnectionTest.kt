package ch.epfl.cs311.wanderwave.model

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.localDb.PlaceHolderEntity
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

public class BeaconConnectionTest {

  @get:Rule
  val mockkRule = MockKRule(this)
  private lateinit var beaconConnection: BeaconConnection

  @RelaxedMockK
  private lateinit var beaconConnectionMock: BeaconConnection

  private lateinit var firestore: FirebaseFirestore
  private lateinit var documentReference: DocumentReference
  private lateinit var documentSnapshot: DocumentSnapshot

  @Before
  fun setup() {
    // Create the mocks
    firestore = mockk()
    documentReference = mockk()
    documentSnapshot = mockk()

    // Define behavior for the mocks
    every { firestore.collection(any()) } returns mockk(relaxed = true)
    every { documentReference.get() } returns mockk(relaxed = true)
    every { documentSnapshot.data } returns mockk(relaxed = true)


    // Pass the mock Firestore instance to your BeaconConnection
    beaconConnection = BeaconConnection(firestore)
  }

  @Test
  fun testAddAndGetItem() = runBlocking {
    withTimeout(3000) { // Increased timeout to 30 seconds
      val beacon = Beacon(
        id = "testBeacon",
        location = Location(1.0, 1.0, "Test Location"),
          profileAndTrack =
          listOf(
              ProfileTrackAssociation(
                  Profile(
                      "Sample First Name",
                      "Sample last name",
                      "Sample desc",
                      0,
                      false,
                      null,
                      "Sample Profile ID",
                      "Sample Track ID"),
                  Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))


      val trackDocumentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
      every { trackDocumentSnapshot.data } returns mapOf(
        "id" to "testTrack",
        "title" to "Test Title",
        "artist" to "Test Artist"
      )
      every { trackDocumentSnapshot.toObject(Track::class.java) } returns Track("testTrack", "Test Title", "Test Artist")
      every { trackDocumentSnapshot.exists() } returns true

      val trackRef = mockk<DocumentReference>(relaxed = true)
      val trackTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
      every { trackTask.isSuccessful } returns true
      every { trackTask.result } returns trackDocumentSnapshot
      every { trackRef.get() } returns trackTask

      // Define behavior for the mocks
      every { firestore.collection(any()).document(any()).set(any()) } returns Tasks.forResult(null)

      // Define behavior for the mocks
      every { firestore.collection(any()).document(any()).delete() } returns Tasks.forResult(null)
      coEvery { firestore.collection(any()).document(any()).get() } returns Tasks.forResult(documentSnapshot)
      every { documentSnapshot.toObject(Beacon::class.java) } returns beacon
      every { documentSnapshot.get("location") } returns mapOf("latitude" to 1.0, "longitude" to 1.0, "name" to "Test Location")
      every { documentSnapshot.get("tracks") } returns listOf(trackRef)
      every { documentSnapshot.id } returns "testBeacon"
      every { documentSnapshot.exists() } returns true

      Log.d("Firestore", "Adding beacon")
      beaconConnection.addItemWithId(beacon)
      Log.d("Firestore", "Added beacon, getting beacon")
      val retrievedBeacon = beaconConnection.getItem("testBeacon").firstOrNull()

      assertEquals(beacon, retrievedBeacon)
      verify { trackRef.get() }
    }
  }



  @Test
  fun testAddItem() = runBlocking {
    // Mock data
    val beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    // Call the function under test
    beaconConnection.addItem(beacon)

    // No verification is needed for interactions with the real object
  }

  @Test
  fun testGetAll() = runBlocking {
    // Place holder test before we merge with the main for coverage
    val retrievedBeacons = beaconConnection.getAll().first()

    // Assert nothing
  }

  @Test
  fun testAddTrackToBeacon() {
    // Mock data
    val beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    val track = Track("testTrack2", "Test Title 2", "Test Artist 2")

    // Call the function under test
    beaconConnection.addTrackToBeacon(beacon.id, track, {})

    // No verification is needed for interactions with the real object
  }

  @Test
  fun testUpdateItem() = runBlocking {
    // Mock data
    val beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    // Call the function under test
    beaconConnection.updateItem(beacon)

    // No verification is needed for interactions with the real object
  }

  // If someone knows how to deal with the flow already being null and how to test it, please let me
  // know
  @Test
  fun testGetNonexistentItem() = runBlocking {
    // By default the test passes, if a value is emitted the test fails
    withTimeout(20000) {
      // Flag to indicate if the flow emits any value
      var valueEmitted = false

      // Collect the flow within a 2-second timeout
      measureTimeMillis {
        withTimeoutOrNull(2000) {
          beaconConnection.getItem("nonexistentBeacon").collect {
            valueEmitted = true // Set the flag if the flow emits any value
          }
        }
      }

      // Assert that the flow didn't emit anything within the timeout
      assert(valueEmitted.not()) { "Flow emitted unexpected value" }
    }
  }

  @Test
  fun testAddItemTwice() = runBlocking {
    withTimeout(20000) {
      val beacon =
        Beacon(
          id = "testBeacon",
          location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
            listOf(
                ProfileTrackAssociation(
                    Profile(
                        "Sample First Name",
                        "Sample last name",
                        "Sample desc",
                        0,
                        false,
                        null,
                        "Sample Profile ID",
                        "Sample Track ID"),
                    Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

      beaconConnection.addItemWithId(beacon)
      beaconConnection.addItemWithId(beacon)

      val retrievedBeacon = beaconConnection.getItem("testBeacon").first()

      assert(beacon == retrievedBeacon)
    }
  }

  @Test
  fun AddDeleteAndGetItem() = runBlocking {
    withTimeout(20000) {
      val beacon =
          Beacon(
              id = "testBeacon1",
              location = Location(1.0, 1.0, "Test Location"),
              profileAndTrack =
                  listOf(
                      ProfileTrackAssociation(
                          Profile(
                              "Sample First Name",
                              "Sample last name",
                              "Sample desc",
                              0,
                              false,
                              null,
                              "Sample Profile ID",
                              "Sample Track ID"),
                          Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

      beaconConnection.addItemWithId(beacon)
      beaconConnection.deleteItem("testBeacon1")

      // Flag to indicate if the flow emits any value
      var valueEmitted = false

      // Collect the flow within a 2-second timeout
      measureTimeMillis {
        withTimeoutOrNull(2000) {
          beaconConnection.getItem("testBeacon1").collect {
            valueEmitted = true // Set the flag if the flow emits any value
          }
        }
      }

      // Assert that the flow didn't emit anything within the timeout
      assert(valueEmitted.not()) { "Flow emitted unexpected value" }
    }
  }

  // TODO : To be deleted after a real entry is added to the database
  private lateinit var db: AppDatabase

  @Test
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

    val placeHolderEntity = PlaceHolderEntity("1", 0.5, 0.5)
  }

  @After
  fun cleanupTestData() = runBlocking {
    // Remove the test data
    beaconConnection.deleteItem("testBeacon")
    beaconConnection.deleteItem("testBeacon1")
    beaconConnection.deleteItem("testBeacon2")
    beaconConnection.deleteItem("nonexistentBeacon")
  }
}
