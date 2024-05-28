[1mdiff --git a/app/src/main/java/ch/epfl/cs311/wanderwave/ui/screens/ProfileScreen.kt b/app/src/main/java/ch/epfl/cs311/wanderwave/ui/screens/ProfileScreen.kt[m
[1mindex 2b3d92c8..5c86d02a 100644[m
[1m--- a/app/src/main/java/ch/epfl/cs311/wanderwave/ui/screens/ProfileScreen.kt[m
[1m+++ b/app/src/main/java/ch/epfl/cs311/wanderwave/ui/screens/ProfileScreen.kt[m
[36m@@ -1,5 +1,6 @@[m
 package ch.epfl.cs311.wanderwave.ui.screens[m
 [m
[32m+[m[32mimport android.util.Log[m
 import androidx.compose.foundation.background[m
 import androidx.compose.foundation.clickable[m
 import androidx.compose.foundation.layout.Arrangement[m
[36m@@ -67,10 +68,12 @@[m [mval INPUT_BOX_NAM_SIZE = 150.dp[m
 @Composable[m
 fun ProfileScreen(navActions: NavigationActions, viewModel: ProfileViewModel, online: Boolean) {[m
   val currentProfileState by viewModel.profile.collectAsState()[m
[31m-  val songLists by viewModel.songLists.collectAsState()[m
[32m+[m[32m  val profile by viewModel.profile.collectAsState()[m
 [m
   val currentProfile: Profile = currentProfileState[m
[31m-  LaunchedEffect(Unit) { viewModel.getProfileOfCurrentUser(true) }[m
[32m+[m[32m  LaunchedEffect(Unit) {[m
[32m+[m[32m      viewModel.getProfileOfCurrentUser(true)[m
[32m+[m[32m  }[m
 [m
   Column([m
       modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profileScreen"),[m
[36m@@ -91,7 +94,7 @@[m [mfun ProfileScreen(navActions: NavigationActions, viewModel: ProfileViewModel, on[m
         }[m
         SongsListDisplay([m
             navigationActions = navActions,[m
[31m-            songLists = songLists,[m
[32m+[m[32m            songLists = profile.topSongs,[m
             onAddTrack = { track -> viewModel.addTrackToList(track) },[m
             onSelectTrack = { track -> viewModel.selectTrack(track) },[m
             viewModelName = viewModelType.PROFILE,[m
[1mdiff --git a/app/src/main/java/ch/epfl/cs311/wanderwave/viewmodel/ProfileViewModel.kt b/app/src/main/java/ch/epfl/cs311/wanderwave/viewmodel/ProfileViewModel.kt[m
[1mindex a12ff7a1..baba903c 100644[m
[1m--- a/app/src/main/java/ch/epfl/cs311/wanderwave/viewmodel/ProfileViewModel.kt[m
[1m+++ b/app/src/main/java/ch/epfl/cs311/wanderwave/viewmodel/ProfileViewModel.kt[m
[36m@@ -1,5 +1,6 @@[m
 package ch.epfl.cs311.wanderwave.viewmodel[m
 [m
[32m+[m[32mimport android.util.Log[m
 import androidx.lifecycle.ViewModel[m
 import androidx.lifecycle.viewModelScope[m
 import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController[m
[36m@@ -43,10 +44,6 @@[m [mconstructor([m
   private val _isInPublicMode = MutableStateFlow(false)[m
   val isInPublicMode: StateFlow<Boolean> = _isInPublicMode[m
 [m
[31m-  // Add a state for managing song lists[m
[31m-  private val _songLists = MutableStateFlow(emptyList<Track>())[m
[31m-  val songLists: StateFlow<List<Track>> = _songLists[m
[31m-[m
   private var _uiState = MutableStateFlow(UIState())[m
   val uiState: StateFlow<UIState> = _uiState[m
 [m
[36m@@ -67,7 +64,9 @@[m [mconstructor([m
         } else {[m
           track[m
         }[m
[31m-    if (!_songLists.value.contains(newTrack)) _songLists.value += mutableListOf(newTrack)[m
[32m+[m[32m    if (!_profile.value.topSongs.contains(newTrack))_profile.value.topSongs+= mutableListOf(newTrack)[m
[32m+[m
[32m+[m[32m    updateProfile(_profile.value)[m
   }[m
 [m
   fun updateProfile(updatedProfile: Profile) {[m
[36m@@ -153,6 +152,7 @@[m [mconstructor([m
     if (wanderwaveLikedTracks.value.contains(track)) _wanderwaveLikedTracks.value -= track[m
   }[m
 [m
[32m+[m
   data class UIState([m
       val profile: Profile? = null,[m
       val isLoading: Boolean = true,[m
[1mdiff --git a/local.defaults.properties b/local.defaults.properties[m
[1mindex 062d0fa6..7cedfc91 100644[m
[1m--- a/local.defaults.properties[m
[1m+++ b/local.defaults.properties[m
[36m@@ -1,2 +1,2 @@[m
[31m-SPOTIFY_CLIENT_ID=PLACEHOLDER[m
[31m-MAPS_API_KEY=PLACEHOLDER[m
\ No newline at end of file[m
[32m+[m[32mSPOTIFY_CLIENT_ID=9db4430e723541eb8518ec0ca059beb4[m
[32m+[m[32mMAPS_API_KEY=AIzaSyCj5GD6TVIZMjn6snBb4VkjhpeuQwPp18M[m
\ No newline at end of file[m
