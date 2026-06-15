package com.fitnessaicoach.app.data.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectSyncManagerTest {

    @Test
    fun `daily activity uses health connect source by default`() {
        val activity = HealthConnectDailyActivity("2026-06-15", 8500, 430)

        assertEquals("health_connect", activity.source)
        assertEquals(8500, activity.steps)
        assertEquals(430, activity.caloriesBurned)
    }

    @Test
    fun `availability enum remains explicit`() {
        assertTrue(HealthConnectAvailability.Available.name.isNotBlank())
        assertTrue(HealthConnectAvailability.NotInstalled.name.isNotBlank())
        assertTrue(HealthConnectAvailability.UpdateRequired.name.isNotBlank())
    }
}
