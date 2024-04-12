package ch.epfl.cs311.wanderwave.viewmodel

import io.mockk.junit4.MockKRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BeaconScreenViewModelTest {

  @get:Rule
  val mockkRule = MockKRule(this)

  @Test
  fun canConstructWithNoErrors() {
    BeaconViewModel()
  }
}