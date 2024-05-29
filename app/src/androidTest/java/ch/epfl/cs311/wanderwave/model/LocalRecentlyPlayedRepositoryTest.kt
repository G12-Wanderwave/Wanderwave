package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.LocalRecentlyPlayedRepository
import ch.epfl.cs311.wanderwave.model.localDb.RecentlyPlayedDao
import ch.epfl.cs311.wanderwave.model.localDb.RecentlyPlayedEntity
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LocalRecentlyPlayedRepositoryTest {

  @get:Rule val mockRule = MockKRule(this)

  @MockK lateinit var recentlyPlayedDao: RecentlyPlayedDao
  @MockK lateinit var trackRepository: TrackRepository

  lateinit var testDispatcher: TestDispatcher
  lateinit var repository: LocalRecentlyPlayedRepository

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    repository = LocalRecentlyPlayedRepository(recentlyPlayedDao, trackRepository, testDispatcher)

    coEvery { recentlyPlayedDao.insertRecentlyPlayed(any()) } just runs
    coEvery { trackRepository.addItemWithId(any()) } just runs

    Dispatchers.setMain(testDispatcher)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun addingPersistsAndAddsToFirebase() = runBlocking {
    val track1 = Track("id1", "title1", "artist1")
    val track2 = Track("id2", "title2", "artist2")

    val instant1 = Instant.ofEpochSecond(1716910000)
    val instant2 = Instant.ofEpochSecond(1716920000)

    every { trackRepository.getItem("id1") } returns flowOf(Result.success(track1))
    every { trackRepository.getItem("id2") } returns flowOf(Result.failure(Exception()))

    repository.addRecentlyPlayed(track2, instant2)

    coVerify {
      recentlyPlayedDao.insertRecentlyPlayed(
          withArg {
            assert(it.trackId == track2.id)
            assert(it.lastPlayed == instant2.epochSecond)
          })
    }
    coVerify(exactly = 1) { trackRepository.addItemWithId(track2) }

    repository.addRecentlyPlayed(track1, instant1)

    coVerify {
      recentlyPlayedDao.insertRecentlyPlayed(
          withArg {
            assert(it.trackId == track1.id)
            assert(it.lastPlayed == instant1.epochSecond)
          })
    }
    coVerify(exactly = 1) { trackRepository.addItemWithId(any()) }
  }

  @Test
  fun gettingRecentlyPlayedPullFromFirebase() = runBlocking {
    val track1 = Track("id1", "title1", "artist1")
    val track2 = Track("id2", "title2", "artist2")

    val instant1 = Instant.ofEpochSecond(1716910000)
    val instant2 = Instant.ofEpochSecond(1716920000)

    every { recentlyPlayedDao.getRecentlyPlayed() } returns
        flowOf(
            listOf(
                RecentlyPlayedEntity("id1", instant1.epochSecond),
                RecentlyPlayedEntity("id2", instant2.epochSecond)))

    every { trackRepository.getItem("id1") } returns flowOf(Result.success(track1))
    every { trackRepository.getItem("id2") } returns flowOf(Result.success(track2))

    val tracks = repository.getRecentlyPlayed().first()

    assert(tracks == listOf(track1, track2))
  }
}
