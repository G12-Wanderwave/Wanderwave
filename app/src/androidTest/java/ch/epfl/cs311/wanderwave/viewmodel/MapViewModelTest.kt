// import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.Context
import android.location.LocationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.LatLng
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class MapViewModelTest {

  @get:Rule val mockkRule = MockKRule(this)

  private val locationSource: LocationSource = mockk()
  private lateinit var beaconRepository: BeaconRepository
  private lateinit var viewModel: MapViewModel

  private lateinit var beaconList: List<Beacon>

  @Before
  fun setup() {
    beaconRepository = mockk()
    beaconList = listOf(Beacon("1", Location(0.0, 0.0, "1")), Beacon("2", Location(0.0, 0.0, "2")))
    every { beaconRepository.getAll() } returns flowOf(beaconList)
    viewModel = MapViewModel(locationSource, beaconRepository)
  }

  @Test
  fun beacons_are_observed_on_initialization() = runBlockingTest {
    val job = launch {
      viewModel.uiState.collect { uiState ->
        assert(uiState.beacons == beaconList)
        assert(!uiState.loading)
      }
    }
    job.cancelAndJoin()
  }

  @Test
  fun cooldown_is_activated_after_dropping_a_track() = runBlockingTest {
    val track = Track("id", "artist", "album")
    val profile = Profile("id", "name", "email", 0, false, null, "spotifyId", "firebaseId")
    val beacon = Beacon("id", Location(0.0, 0.0, "name"))

    every { locationSource.activate(any()) } answers
        {
          val listener = arg<LocationSource.OnLocationChangedListener>(0)
          listener.onLocationChanged(
              android.location.Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 0.0
                longitude = 0.0
              }) // Provide a Location object to the callback
        }

    viewModel.isInBeaconRange(track, profile)
  }

  @Test
  fun last_known_location_is_returned_when_permissions_are_granted() = runBlockingTest {
    val context: Context = mockk()
    val locationManager: LocationManager = mockk()
    val location: android.location.Location = mockk()

    every { context.getSystemService(Context.LOCATION_SERVICE) } returns locationManager
    every { locationManager.getProviders(true) } returns listOf(LocationManager.GPS_PROVIDER)
    every { locationManager.getLastKnownLocation(any()) } returns location
    every { location.latitude } returns 0.0
    every { location.longitude } returns 0.0

    val result = viewModel.getLastKnownLocation(context)

    assert(result == LatLng(0.0, 0.0))
  }
}
