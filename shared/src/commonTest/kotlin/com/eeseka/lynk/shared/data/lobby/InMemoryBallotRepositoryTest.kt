package com.eeseka.lynk.shared.data.lobby

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.testing.data.FakeSessionStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class InMemoryBallotRepositoryTest {

    private lateinit var sessionStorage: FakeSessionStorage

    private val lounge = spot(id = "spot_1")
    private val rooftop = spot(id = "spot_2")

    @BeforeTest
    fun setUp() {
        sessionStorage = FakeSessionStorage()
    }

    @Test
    fun `a ballot is empty until the lobby reports one`() = runTest {
        val repository = createRepository()

        assertThat(repository.observeCandidates(HANGOUT_ID).first()).isEmpty()
    }

    @Test
    fun `replacing the ballot keeps exactly the new candidates`() = runTest {
        val repository = createRepository()
        repository.replaceCandidates(HANGOUT_ID, listOf(lounge))

        repository.replaceCandidates(HANGOUT_ID, listOf(rooftop))

        assertThat(repository.observeCandidates(HANGOUT_ID).first().map { it.id }).containsExactly("spot_2")
    }

    @Test
    fun `adding a spot already on the ballot keeps one copy`() = runTest {
        val repository = createRepository()
        repository.replaceCandidates(HANGOUT_ID, listOf(lounge))

        repository.addCandidate(HANGOUT_ID, lounge)
        repository.addCandidate(HANGOUT_ID, rooftop)

        assertThat(repository.observeCandidates(HANGOUT_ID).first().map { it.id }).containsExactly("spot_1", "spot_2")
    }

    @Test
    fun `removing a spot takes only that spot off`() = runTest {
        val repository = createRepository()
        repository.replaceCandidates(HANGOUT_ID, listOf(lounge, rooftop))

        repository.removeCandidate(HANGOUT_ID, "spot_1")

        assertThat(repository.observeCandidates(HANGOUT_ID).first().map { it.id }).containsExactly("spot_2")
    }

    @Test
    fun `ballots are kept apart by hangout id`() = runTest {
        val repository = createRepository()

        repository.replaceCandidates(HANGOUT_ID, listOf(lounge))

        assertThat(repository.observeCandidates(OTHER_HANGOUT_ID).first()).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a change of user clears every ballot`() = runTest {
        sessionStorage.set(guestSession(userId = "user_a"))
        val repository = createRepository()
        repository.replaceCandidates(HANGOUT_ID, listOf(lounge))

        sessionStorage.set(guestSession(userId = "user_b"))
        runCurrent()

        assertThat(repository.observeCandidates(HANGOUT_ID).first()).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createRepository(): InMemoryBallotRepository {
        val repository = InMemoryBallotRepository(sessionStorage, backgroundScope)
        runCurrent()
        return repository
    }

    private fun guestSession(userId: String) = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = User.Guest(id = userId)
    )

    private fun spot(id: String) = Spot(
        id = id, name = "Spot $id", category = SpotCategory.CAFE,
        latitude = 6.5, longitude = 3.3, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 100,
        openingHours = null, shortAddress = "VI, Lagos",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, savedAt = null,
        typeLabel = null, generativeSummary = null, reviewSummary = null, priceRange = null,
        businessStatus = null, amenities = null, parking = null, payment = null,
        phoneNumber = null, directionsUrl = null
    )

    private companion object {
        const val HANGOUT_ID = "hangout_1"
        const val OTHER_HANGOUT_ID = "hangout_2"
    }
}
