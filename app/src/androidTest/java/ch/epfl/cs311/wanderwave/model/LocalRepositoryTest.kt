package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.local.BeaconDao
import ch.epfl.cs311.wanderwave.model.local.BeaconEntity
import ch.epfl.cs311.wanderwave.model.local.LocalBeaconRepository
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.localDb.LocalProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepositoryImpl
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocalProfileRepositoryTest {

  @get:Rule val mockkRule = MockKRule(this)
  @RelaxedMockK private lateinit var profileLocalRepository: LocalProfileRepository
  private lateinit var profileRepository: ProfileRepositoryImpl

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    profileRepository = ProfileRepositoryImpl(profileLocalRepository)
  }

  @Test
  fun ProfileRepositoryImpl_getProfile() {
    // Mock data
    /*
        * public constructor Profile(
        val firstName: String,
        val lastName: String,
        val description: String,
        val numberOfLikes: Int,
        val isPublic: Boolean,
        val profilePictureUri: Uri? = null,
        val spotifyUid: String,
        val firebaseUid: String
    )*/
    val profile =
        Profile("Alice", "Franc", "I am a student", 0, true, null, "spotifyUid", "firebaseUid")

    // Mock behavior of local profile repository
    coEvery { profileLocalRepository.getProfile() } returns flowOf(profile)

    // Call the method under test
    val result = runBlocking {
      profileRepository.getProfile().first() // Get the first emitted value
    }

    // Check the result
    assertEquals(profile, result)
  }

  @Test
  fun ProfileRepositoryImpl_insert() {
    // Mock data
    val profile =
        Profile("Alice", "Franc", "I am a student", 0, true, null, "spotifyUid", "firebaseUid")

    // Call the method under test
    runBlocking { profileRepository.insert(profile) }

    // Verify that insert method of local profile repository is called with correct arguments
    coEvery { profileLocalRepository.insert(profile) }
  }

  @Test
  fun ProfileRepositoryImpl_delete() {
    // Call the method under test
    runBlocking { profileRepository.delete() }

    // Verify that delete method of local profile repository is called
    coEvery { profileLocalRepository.delete() }
  }
}

class LocalBeaconRepositoryTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var beaconDao: BeaconDao

  private lateinit var beaconLocalRepository: LocalBeaconRepository
  @RelaxedMockK private lateinit var beaconLocalRepositoryMock: LocalBeaconRepository

  private lateinit var beaconRepository: BeaconRepositoryImpl
  private lateinit var beaconRepositoryMock: BeaconRepositoryImpl

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    // Mocking the AppDatabase and providing the mocked beaconDao
    val database = mockk<AppDatabase>()
    every { database.beaconDao() } returns beaconDao

    // Creating the repository with the mocked database
    beaconLocalRepository = LocalBeaconRepository(database)

    beaconRepository = BeaconRepositoryImpl(beaconLocalRepository)
    beaconRepositoryMock = BeaconRepositoryImpl(beaconLocalRepositoryMock)
  }

  @Test
  fun BeaconRepositoryImplgetAll() {
    // Mock data
    val beaconList =
        listOf(
            Beacon("id1", Location(1.0, 1.0), listOf<Track>()),
            Beacon("id2", Location(2.0, 2.0), listOf<Track>()),
        )

    // Mock behavior of local beacon repository
    coEvery { beaconLocalRepositoryMock.getAll() } returns flowOf(beaconList)

    // Call the method under test
    val result = runBlocking {
      beaconRepositoryMock.getAll().toList() // Collect flow into a list
    }

    // Check the result
    assertEquals(listOf(beaconList), result)
  }

  @Test
  fun beaconEntityIsCorrectlyInitialized() {
    val id = "testId"
    val latitude = 1.0
    val longitude = 1.0

    val beaconEntity = BeaconEntity(id, latitude, longitude)

    assertEquals(id, beaconEntity.id)
    assertEquals(latitude, beaconEntity.latitude, 0.0)
    assertEquals(longitude, beaconEntity.longitude, 0.0)
  }

  @Test
  fun testGetAll() = runBlocking {
    val beacons =
        listOf(
            BeaconEntity("1", 37.7749, -122.4194), // San Francisco
            BeaconEntity("2", 40.7128, -74.0060) // New York
            )

    // Mocking the behavior of getAll() in beaconDao
    every { beaconDao.getAll() } returns beacons

    fun BeaconEntity.toBeacon() = Beacon(id, Location(latitude, longitude))

    // Retrieving the list of beacons from the repository
    val result = beaconLocalRepository.getAll().first()

    // Asserting that the result matches the expected list of beacons
    assert(result.size == beacons.size)
    assert(result.map { it } == beacons.map { it.toBeacon() })
  }
}
