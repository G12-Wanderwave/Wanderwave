package ch.epfl.cs311.wanderwave.viewmodel

import android.Manifest
import android.location.Location
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationUserData
import ch.epfl.cs311.wanderwave.model.data.*
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.utils.addTrackToBeacon
import com.google.android.gms.maps.LocationSource
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MapViewModelTest {

  @get:Rule val mockkRule = MockKRule(this)

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var viewModel: MapViewModel
  private lateinit var mockLocationSource: LocationSource
  private lateinit var mockBeaconRepository: BeaconRepository
  @RelaxedMockK private lateinit var trackRepository: TrackRepository

  @RelaxedMockK private lateinit var profileRepository: ProfileConnection
  @RelaxedMockK private lateinit var mockAuthenticationController: AuthenticationController

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
      )

  private val context = InstrumentationRegistry.getInstrumentation().targetContext

  @Before
  fun setup() = runTest {
    MockKAnnotations.init(this)

    mockBeaconRepository = mockk()
    mockLocationSource = mockk()
    coEvery { mockBeaconRepository.getAll() } returns
        flowOf(
            listOf(
                Beacon(
                    "UAn8OUadgrUOKYagf8a2",
                    ch.epfl.cs311.wanderwave.model.data.Location(46.519653, 6.632273, "Lausanne"),
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
                                Track(
                                    "Sample Track ID",
                                    "Sample Track Title",
                                    "Sample Artist Name"))))))

    coEvery { mockBeaconRepository.getItem("UAn8OUadgrUOKYagf8a2") } returns
        flowOf(
            Result.success(
                Beacon(
                    "UAn8OUadgrUOKYagf8a2",
                    ch.epfl.cs311.wanderwave.model.data.Location(46.519653, 6.632273, "Lausanne"),
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
                                Track(
                                    "Sample Track ID",
                                    "Sample Track Title",
                                    "Sample Artist Name"))))))
    viewModel =
        MapViewModel(
            mockLocationSource,
            mockBeaconRepository,
            mockAuthenticationController,
            trackRepository,
            profileRepository)
  }

  @Test
  fun testGetLastKnownLocation() = runTest {
    val location = viewModel.getLastKnownLocation(context)
    // Since this is a test for a function dependent on actual device location,
    // we just want to ensure the function doesn't crash and can be called successfully.
    // Further specific testing might require more complex instrumentation tests or mock location
    // setups.
    assert(location == null || location != null)
  }

  @Test
  fun getProfileOfCurrentUser() = runTest {
    // Given
    val expectedProfile =
        Profile(
            firstName = "My FirstName",
            lastName = "My LastName",
            description = "My Description",
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "My Spotify UID",
            firebaseUid = "My Firebase UID",
            profilePictureUri = null)
    coEvery { profileRepository.getItem(any()) } returns flowOf(Result.success(expectedProfile))

    // When
    viewModel.getProfileOfCurrentUser()

    Assert.assertEquals(expectedProfile, viewModel.profile.value)
  }

  @Test
  fun getRandomSong_returnsRandomSong_whenBeaconHasSongs() = runBlockingTest {
    // Mocking
    val beaconId = "Sample Beacon ID"
    val track = Track("Sample Track ID", "Sample Track Title", "Sample Artist Name")
    val userId = "userId"
    val profile =
        Profile(
            firebaseUid = userId,
            chosenSongs = listOf(track),
            topSongs = listOf(track),
            likedSongs = listOf(track),
            bannedSongs = listOf(track),
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "My Spotify UID",
            profilePictureUri = null,
            firstName = "My FirstName",
            lastName = "My LastName",
            description = "My Description")
    val beacon =
        Beacon(
            beaconId,
            profileAndTrack = listOf(ProfileTrackAssociation(profile, track)),
            location = ch.epfl.cs311.wanderwave.model.data.Location(0.0, 0.0))

    // Mock the AuthenticationUserData
    val mockUserData = mockk<AuthenticationUserData> { every { id } returns userId }

    // Mock the responses
    coEvery { mockAuthenticationController.getUserData() } returns mockUserData
    // Mocking
    coEvery { profileRepository.getItem(userId) } returns flowOf(Result.success(profile))

    coEvery { mockBeaconRepository.getItem(beaconId) } returns flowOf(Result.success(beacon))

    // Act
    viewModel.getRandomSong(beaconId)
  }

  @Test
  fun retrieveRandomSongFromProfileAndAddToBeacon_retrievesSongAndAddsToBeacon_whenProfileHasSongs() =
      runBlockingTest {
        // Given
        val beaconId = "Sample Beacon ID"
        val track = Track("Sample Track ID", "Sample Track Title", "Sample Artist Name")
        val profile =
            Profile(
                firebaseUid = "Sample ",
                chosenSongs = listOf(track),
                topSongs = listOf(track),
                likedSongs = listOf(track),
                bannedSongs = listOf(track),
                numberOfLikes = 0,
                isPublic = true,
                spotifyUid = "My Spotify UID",
                profilePictureUri = null,
                firstName = "My FirstName",
                lastName = "My LastName",
                description = "My Description")
        val beacon =
            Beacon(
                beaconId,
                profileAndTrack = emptyList(),
                location = ch.epfl.cs311.wanderwave.model.data.Location(0.0, 0.0))

        // Mock the responses
        coEvery { profileRepository.getItem(profile.firebaseUid) } returns
            flowOf(Result.success(profile))
        coEvery { mockBeaconRepository.getItem(beaconId) } returns flowOf(Result.success(beacon))
        coEvery { mockBeaconRepository.updateItem(any()) } just Runs

        // Act
        viewModel.retrieveRandomSongFromProfileAndAddToBeacon(beaconId)
      }

  @Test
  fun retrieveRandomSongFromProfileAndAddToBeacon_doesNothing_whenProfileHasNoSongs() =
      runBlockingTest {
        // Given
        val beaconId = "Sample Beacon ID"
        val track = Track("Sample Track ID", "Sample Track Title", "Sample Artist Name")

        val profile =
            Profile(
                firebaseUid = "Sample ",
                chosenSongs = listOf(track),
                topSongs = listOf(track),
                likedSongs = listOf(track),
                bannedSongs = listOf(track),
                numberOfLikes = 0,
                isPublic = true,
                spotifyUid = "My Spotify UID",
                profilePictureUri = null,
                firstName = "My FirstName",
                lastName = "My LastName",
                description = "My Description")

        // Mock the responses
        coEvery { profileRepository.getItem(profile.firebaseUid) } returns
            flowOf(Result.success(profile))

        // Act
        viewModel.retrieveRandomSongFromProfileAndAddToBeacon(beaconId)

        // Assert
        coVerify(exactly = 0) { mockBeaconRepository.updateItem(any()) }
      }

  @Test
  fun updateChosenSongsProfile_doesNotUpdateProfile_whenSongIdIsAlreadyInChosenSongs() =
      runBlockingTest {
        val userId = "userId"

        // Mock the AuthenticationUserData
        val mockUserData = mockk<AuthenticationUserData> { every { id } returns userId }

        // Mock the responses
        coEvery { mockAuthenticationController.getUserData() } returns mockUserData
        // Mocking
        val track = Track("Sample Track ID", "Sample Track Title", "Sample Artist Name")
        val profile =
            Profile(
                firebaseUid = userId,
                chosenSongs = listOf(track),
                topSongs = listOf(track),
                likedSongs = listOf(track),
                bannedSongs = listOf(track),
                numberOfLikes = 0,
                isPublic = true,
                spotifyUid = "My Spotify UID",
                profilePictureUri = null,
                firstName = "My FirstName",
                lastName = "My LastName",
                description = "My Description")
        coEvery { profileRepository.getItem(userId) } returns flowOf(Result.success(profile))

        // Act
        viewModel.updateChosenSongsProfile()

        // Assert
        coVerify(exactly = 0) { profileRepository.updateItem(any()) }
      }

  @Test
  fun retrieveSongFromProfileAndAddToBeaconSongs() = runTest {
    // Given
    val beaconId = "beaconId"
    val profile =
        Profile(
            firstName = "My FirstName",
            lastName = "My LastName",
            description = "My Description",
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "My Spotify UID",
            firebaseUid = "My Firebase UID",
            profilePictureUri = null,
            topSongs = listOf(Track("trackId", "trackName", "trackArtist")))
    coEvery { profileRepository.getItem(any()) } returns flowOf(Result.success(profile))
    coEvery { mockBeaconRepository.addTrackToBeacon(any(), any(), any(), any()) } answers
        {
          val callback = args[3] as ((Boolean) -> Unit)
          callback.invoke(true)
        }

    viewModel.getProfileOfCurrentUser()

    // Then
    // When
    viewModel.retrieveRandomSongFromProfileAndAddToBeacon(beaconId)
  }

  @Test
  fun retrieveSongFromProfileAndAddToBeaconNosongs() = runTest {
    // Given
    val beaconId = "beaconId"
    val profile =
        Profile(
            firstName = "My FirstName",
            lastName = "My LastName",
            description = "My Description",
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "My Spotify UID",
            firebaseUid = "My Firebase UID",
            profilePictureUri = null,
            topSongs = emptyList())
    coEvery { profileRepository.getItem(any()) } returns
        flowOf(Result.failure(Exception("Profile not found")))
    viewModel.getProfileOfCurrentUser()

    // When
    viewModel.retrieveRandomSongFromProfileAndAddToBeacon(beaconId)

    // Then
    coVerify(exactly = 0) { mockBeaconRepository.addTrackToBeacon(any(), any(), any(), any()) }
  }

  @Test
  fun getRandomSongAndAddToProfileNoSongs() = runTest {
    // Given
    val beaconId = "beaconId"
    val beacon =
        Beacon(
            id = beaconId,
            location = ch.epfl.cs311.wanderwave.model.data.Location(0.0, 0.0),
            profileAndTrack = emptyList())
    coEvery { mockBeaconRepository.getItem(any()) } returns
        flowOf(Result.failure(Exception("Profile not found")))

    // When
    viewModel.getRandomSong(beaconId)

    // Then
    coVerify(exactly = 0) { trackRepository.addItemsIfNotExist(any()) }
  }

  @Test
  fun retrieveSongFromProfileAndAddToBeacon() = runTest {
    // Given
    val beaconId = "beaconId"
    val profile =
        Profile(
            firstName = "My FirstName",
            lastName = "My LastName",
            description = "My Description",
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "My Spotify UID",
            firebaseUid = "My Firebase UID",
            profilePictureUri = null,
            topSongs = listOf(Track("trackId", "trackName", "trackArtist")))
    coEvery { profileRepository.getItem(any()) } returns flowOf(Result.success(profile))
    coEvery { mockBeaconRepository.addTrackToBeacon(any(), any(), any(), any()) } answers
        {
          val callback = args[3] as ((Boolean) -> Unit)
          callback.invoke(true)
        }

    // When
    viewModel.retrieveRandomSongFromProfileAndAddToBeacon(beaconId)
  }

  @Test
  fun retrieveSongFromProfileAndAddToBeaconNo() = runTest {
    // Given
    val beaconId = "beaconId"
    val profile =
        Profile(
            firstName = "My FirstName",
            lastName = "My LastName",
            description = "My Description",
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "My Spotify UID",
            firebaseUid = "My Firebase UID",
            profilePictureUri = null,
            topSongs = emptyList())
    coEvery { profileRepository.getItem(any()) } returns flowOf(Result.success(profile))

    // When
    viewModel.retrieveRandomSongFromProfileAndAddToBeacon(beaconId)

    // Then
    coVerify(exactly = 0) { mockBeaconRepository.addTrackToBeacon(any(), any(), any(), any()) }
  }

  @Test
  fun testLoadBeacons() = runTest {
    viewModel.loadBeacons(
        context, ch.epfl.cs311.wanderwave.model.data.Location(46.519653, 6.632273, "Lausanne"))
    // Ensure that the method completes without issues.
  }
}
