package ch.epfl.cs311.wanderwave.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

public class BeaconConnectionTest {

    @get:Rule val mockkRule = MockKRule(this)
    private lateinit var beaconConnection: BeaconConnection

    @RelaxedMockK private lateinit var beaconConnectionMock: BeaconConnection
    private lateinit var trackConnection: TrackConnection
    private lateinit var profileConnection: ProfileConnection // Add a mock for ProfileConnection

    private lateinit var firestore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference
    private lateinit var collectionReference: CollectionReference

    lateinit var beacon: Beacon

    @Before
    fun setup() {
        // Create the mocks
        firestore = mockk()
        documentReference = mockk<DocumentReference>(relaxed = true)
        collectionReference = mockk<CollectionReference>(relaxed = true)
        trackConnection = mockk<TrackConnection>(relaxed = true)
        profileConnection = mockk<ProfileConnection>(relaxed = true)

        // Mock data
        beacon =
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

        // Define behavior for the mocks
        every { collectionReference.document(beacon.id) } returns documentReference
        every { firestore.collection(any()) } returns collectionReference

        // Pass the mock Firestore instance to your BeaconConnection
        beaconConnection = BeaconConnection(firestore, trackConnection, profileConnection)
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