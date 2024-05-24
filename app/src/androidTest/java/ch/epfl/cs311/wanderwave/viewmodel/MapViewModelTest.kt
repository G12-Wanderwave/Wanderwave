package ch.epfl.cs311.wanderwave.viewmodel

import android.Manifest
import android.location.Location
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import com.google.android.gms.maps.LocationSource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
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

  }

  @Test
  fun testLoadBeacons() = runTest {
    viewModel.loadBeacons(
        context, ch.epfl.cs311.wanderwave.model.data.Location(46.519653, 6.632273, "Lausanne"))
  }
  // @Test fun testGetRandomSong() = runTest { viewModel.getRandomSong("UAn8OUadgrUOKYagf8a2") }
}
