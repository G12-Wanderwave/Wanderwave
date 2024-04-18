package ch.epfl.cs311.wanderwave.ui

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.location.FastLocationSource
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.MapScreen
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.android.gms.maps.LocationSource
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions
  @RelaxedMockK private lateinit var mockNavController: NavHostController

  @RelaxedMockK private lateinit var mockLocationSource: LocationSource
  @RelaxedMockK private lateinit var mockMapViewModel: MapViewModel

  @RelaxedMockK private lateinit var mockBeaconConnection: BeaconConnection

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
      )

  private val context = InstrumentationRegistry.getInstrumentation().context

  private val locationManager =
      context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

  val location =
      Location(LocationManager.GPS_PROVIDER).apply {
        latitude = 46.519962
        longitude = 6.633597
        time = System.currentTimeMillis()
        elapsedRealtimeNanos = System.nanoTime()
      }

  @Before
  fun setup() {
    MockKAnnotations.init(this)

    coEvery { mockBeaconConnection.getAll() } returns
        flowOf(
            listOf(
                Beacon(
                    "UAn8OUadgrUOKYagf8a2",
                    ch.epfl.cs311.wanderwave.model.data.Location(46.519653, 6.632273, "Lausanne"),
                    listOf<Track>(Track("Some Track ID", "Track Title", "Artist Name")),
                )))

    mockMapViewModel = MapViewModel(mockLocationSource, mockBeaconConnection)
    every { mockNavController.navigate(any<String>()) } returns Unit
    mockNavigationActions = NavigationActions(mockNavController)

    composeTestRule.setContent { MapScreen(mockNavigationActions, mockMapViewModel) }

    try {
      locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
      locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
    } catch (e: SecurityException) {
      e.printStackTrace()
    }
  }

  @Test
  fun launchingMapScreenDoesNotThrowError() = run {
    onComposeScreen<MapScreen>(composeTestRule) { assertIsDisplayed() }
  }

  @Test
  fun locationSourceCallsActivate() = run {
    val viewModel = mockMapViewModel
    verify { viewModel.locationSource.activate(any()) }
  }

  @Test
  fun listenerIsNullAfterDeactivate() = run {
    val locationSource = FastLocationSource(context)
    locationSource.deactivate()
    assert(locationSource.listener == null)
  }

  @Test
  fun locationSourceCallsOnLocationChanged() = run {
    val mockListener = mockk<LocationSource.OnLocationChangedListener>()
    every { mockListener.onLocationChanged(any()) } returns Unit
    val mockContext = mockk<Context>(relaxed = true)
    val locationManager = mockk<LocationManager>()
    every { mockContext.getSystemService(Context.LOCATION_SERVICE) } returns locationManager
    every {
      locationManager.requestLocationUpdates(
          any<String>(), any<Long>(), any(), any<LocationListener>())
    } returns Unit
    val source = FastLocationSource(mockContext)
    Looper.prepare()
    source.activate(mockListener)
    source.onLocationChanged(location)
    verify { mockListener.onLocationChanged(any()) }
  }

  @Test
  fun map_is_display_and_not_circular() = run {
    ComposeScreen.onComposeScreen<MapScreen>(composeTestRule) {
      circularProgressIndicator { assertIsNotDisplayed() } // This line is the difference
      assertIsDisplayed()
    }
  }

  @Test
  fun canNavigateToBeaconScreen() = run {
    mockNavigationActions.navigateToBeacon("abc")
    verify { mockNavController.navigate("${Route.BEACON.routeString}/abc") }
  }
}
