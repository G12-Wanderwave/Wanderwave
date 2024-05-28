package ch.epfl.cs311.wanderwave.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.navigation.Route
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationActionsTest {
  @Test
  fun testGetRouteForRouteString() {
    assertEquals(null, Route.forRouteString("invalid"))

    assertEquals(Route.LOGIN, Route.forRouteString("login"))
    assertEquals(Route.SPOTIFY_CONNECT, Route.forRouteString("spotifyConnect"))
    assertEquals(Route.MAIN, Route.forRouteString("main"))
    assertEquals(Route.TRACK_LIST, Route.forRouteString("trackList"))
    assertEquals(Route.MAP, Route.forRouteString("map"))
    assertEquals(Route.PROFILE, Route.forRouteString("profile"))
    assertEquals(Route.EDIT_PROFILE, Route.forRouteString("editprofile"))
    assertEquals(Route.VIEW_PROFILE, Route.forRouteString("viewProfile"))
    assertEquals(Route.ABOUT, Route.forRouteString("about"))

    assertEquals(Route.SELECT_SONG, Route.forRouteString("selectsong"))
    assertEquals(Route.SELECT_SONG, Route.forRouteString("selectsong/abc"))
    assertEquals(Route.BEACON, Route.forRouteString("beacon"))
    assertEquals(Route.BEACON, Route.forRouteString("beacon/abc"))
  }
}
