package com.fitnessaicoach.app.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class HealthConnectDailyActivity(
    val date: String,
    val steps: Int,
    val caloriesBurned: Int,
    val source: String = "health_connect",
)

enum class HealthConnectAvailability {
    Available,
    NotInstalled,
    UpdateRequired,
}

@Singleton
class HealthConnectSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
    )

    fun permissions(): Set<String> = permissions

    fun availability(): HealthConnectAvailability {
        val sdkStatusMethod =
            HealthConnectClient::class.java.methods.firstOrNull { it.name == "getSdkStatus" }
                ?: return HealthConnectAvailability.Available
        val status = (sdkStatusMethod.invoke(null, context) as? Int) ?: return HealthConnectAvailability.Available
        return when (status) {
            3 -> HealthConnectAvailability.Available
            2 -> HealthConnectAvailability.UpdateRequired
            else -> HealthConnectAvailability.NotInstalled
        }
    }

    suspend fun hasPermissions(): Boolean {
        if (availability() != HealthConnectAvailability.Available) return false
        val granted = HealthConnectClient.getOrCreate(context).permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readTodayActivity(): Result<HealthConnectDailyActivity> = runCatching {
        require(availability() == HealthConnectAvailability.Available) {
            "Health Connect no está disponible en este dispositivo."
        }
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val range = TimeRangeFilter.between(
            today.atStartOfDay(zone).toInstant(),
            today.plusDays(1).atStartOfDay(zone).toInstant(),
        )
        val client = HealthConnectClient.getOrCreate(context)
        val steps = client.readRecords(ReadRecordsRequest(StepsRecord::class, range)).records.sumOf { it.count }.toInt()
        val calories = client.readRecords(ReadRecordsRequest(ActiveCaloriesBurnedRecord::class, range)).records
            .sumOf { it.energy.inKilocalories.toDouble() }
            .toInt()

        if (steps == 0 && calories == 0) {
            error("No hay datos de actividad para hoy en Health Connect.")
        }

        HealthConnectDailyActivity(today.toString(), steps, calories)
    }
}
