package ch.epfl.cs311.wanderwave.ui

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.model.location.FastLocationSource
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import ch.epfl.cs311.wanderwave.ui.screens.MapScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.android.gms.maps.LocationSource
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK private lateinit var mockMapViewModel: MapViewModel

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
      )

  private val locationManager =
      InstrumentationRegistry.getInstrumentation()
          .context
          .getSystemService(Context.LOCATION_SERVICE) as LocationManager

  @Before
  fun setup() {
    composeTestRule.setContent {
      MapScreen(mockNavigationActions, mockMapViewModel)
    }

    val location =
        Location(LocationManager.GPS_PROVIDER).apply {
          latitude = 46.519962
          longitude = 6.633597
          time = System.currentTimeMillis()
          elapsedRealtimeNanos = System.nanoTime()
        }
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
  fun locationCallbackIsCalled() = run {
    val viewModel = mockMapViewModel
    verify {
      viewModel.locationSource
    }
  }
}
