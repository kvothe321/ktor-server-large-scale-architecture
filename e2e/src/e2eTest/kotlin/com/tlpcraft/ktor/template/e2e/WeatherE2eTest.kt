package com.tlpcraft.ktor.template.e2e

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class WeatherE2eTest {
    private val client = e2eClient()

    @AfterTest
    fun close() = client.close()

    @Test
    fun `current weather is fetched live, then served from the cache`() = runTest {
        // Random coordinates (two decimals, the cache granularity), so earlier runs against a
        // long-lived server cannot have cached them already.
        val latitude = Random.nextInt(-8_000, 8_000) / 100.0
        val longitude = Random.nextInt(-17_000, 17_000) / 100.0
        val url = "api/v1/weather/current?latitude=$latitude&longitude=$longitude"

        val first = client.get(url)
        assertEquals(HttpStatusCode.OK, first.status)
        val live = first.body<Weather>()
        assertEquals("LIVE", live.freshness)
        assertEquals(latitude, live.latitude)
        assertEquals(longitude, live.longitude)
        assertTrue(live.windSpeedKmh >= 0)
        assertTrue(live.condition.isNotBlank())

        val cached = client.get(url).body<Weather>()
        assertEquals("CACHED", cached.freshness)
        assertEquals(live.fetchedAt, cached.fetchedAt)
    }

    @Test
    fun `missing or out of range coordinates are rejected`() = runTest {
        val missing = client.get("api/v1/weather/current").body<Problem>()
        val outOfRange = client.get("api/v1/weather/current?latitude=91&longitude=181").body<Problem>()

        assertEquals(listOf("latitude", "longitude"), missing.violations.map { it.field })
        assertEquals(listOf("latitude", "longitude"), outOfRange.violations.map { it.field })
    }
}
