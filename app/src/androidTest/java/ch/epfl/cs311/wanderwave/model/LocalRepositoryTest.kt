package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.local.BeaconEntity
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocalRepositoryTest {

  @get:Rule val mockkRule = MockKRule(this)
  @RelaxedMockK private lateinit var repository: ProfileRepositoryImpl

  @Before fun setup() {}

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
}
