package com.fitnessaicoach.app.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.*

// ── Auth ───────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class AuthRequest(@Json(name = "email") val email: String, @Json(name = "password") val password: String)

@JsonClass(generateAdapter = true)
data class AuthResponse(@Json(name = "token") val token: String)

// ── Profile ────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class MetabolicProfile(
    @Json(name = "age")                val age: Int,
    @Json(name = "sex")                val sex: String,
    @Json(name = "heightCm")           val heightCm: Double,
    @Json(name = "currentWeightKg")    val currentWeightKg: Double,
    @Json(name = "activityLevel")      val activityLevel: String,
    @Json(name = "goal")               val goal: String,
    @Json(name = "dietType")           val dietType: String,
    @Json(name = "weeklyExerciseDays") val weeklyExerciseDays: Int? = null,
    @Json(name = "exerciseMinutes")    val exerciseMinutes: Int? = null,
    @Json(name = "dailySteps")         val dailySteps: Int? = null,
)

@JsonClass(generateAdapter = true)
data class NutritionTarget(
    @Json(name = "calories")      val calories: Double,
    @Json(name = "proteinG")      val proteinG: Double,
    @Json(name = "carbsG")        val carbsG: Double,
    @Json(name = "fatG")          val fatG: Double,
    @Json(name = "calculatedAt")  val calculatedAt: String? = null,
)

// ── Weight ─────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class WeightLogRequest(@Json(name = "weightKg") val weightKg: Double, @Json(name = "loggedAt") val loggedAt: String? = null)

@JsonClass(generateAdapter = true)
data class WeightLog(@Json(name = "id") val id: Long, @Json(name = "weightKg") val weightKg: Double, @Json(name = "loggedAt") val loggedAt: String)

// ── Activity ───────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class ActivityLogRequest(
    @Json(name = "date")            val date: String,
    @Json(name = "steps")           val steps: Int? = null,
    @Json(name = "caloriesBurned")  val caloriesBurned: Int? = null,
    @Json(name = "notes")           val notes: String? = null,
)

// ── Dashboard ──────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class WeightPoint(@Json(name = "loggedAt") val loggedAt: String, @Json(name = "weightKg") val weightKg: Double)

@JsonClass(generateAdapter = true)
data class WeeklySummary(
    @Json(name = "stepsTotal")          val stepsTotal: Long,
    @Json(name = "caloriesBurnedTotal") val caloriesBurnedTotal: Long,
    @Json(name = "daysLogged")          val daysLogged: Int,
    @Json(name = "avgSteps")            val avgSteps: Double,
    @Json(name = "weightDelta")         val weightDelta: Double?,
)

@JsonClass(generateAdapter = true)
data class TodaySnapshot(
    @Json(name = "steps")               val steps: Long,
    @Json(name = "caloriesBurned")      val caloriesBurned: Long,
    @Json(name = "currentWeightKg")     val currentWeightKg: Double?,
    @Json(name = "targetCalories")      val targetCalories: Double?,
)

// ── Chat ───────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class Conversation(@Json(name = "id") val id: Long, @Json(name = "title") val title: String, @Json(name = "createdAt") val createdAt: String)

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "id")         val id: Long,
    @Json(name = "role")       val role: String,
    @Json(name = "content")    val content: String,
    @Json(name = "createdAt")  val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(@Json(name = "content") val content: String)

// ── API interface ──────────────────────────────────────────────
interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body req: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body req: AuthRequest): AuthResponse

    // Profile
    @GET("profile/me")
    suspend fun getProfile(): MetabolicProfile

    @PUT("profile/me")
    suspend fun saveProfile(@Body profile: MetabolicProfile): MetabolicProfile

    @GET("profile/targets")
    suspend fun getTargets(): NutritionTarget

    // Weights
    @POST("weights")
    suspend fun addWeight(@Body req: WeightLogRequest): WeightLog

    @GET("weights")
    suspend fun getWeights(@Query("from") from: String? = null, @Query("to") to: String? = null): List<WeightLog>

    @DELETE("weights/{id}")
    suspend fun deleteWeight(@Path("id") id: Long)

    // Activities
    @POST("activities")
    suspend fun logActivity(@Body req: ActivityLogRequest)

    @GET("activities")
    suspend fun getActivities(@Query("from") from: String? = null, @Query("to") to: String? = null): List<ActivityLogRequest>

    @GET("activities/today")
    suspend fun getToday(): ActivityLogRequest

    // Dashboard
    @GET("dashboard/weight-progress")
    suspend fun weightProgress(@Query("days") days: Int = 90): List<WeightPoint>

    @GET("dashboard/weekly-summary")
    suspend fun weeklySummary(): WeeklySummary

    @GET("dashboard/today")
    suspend fun dashboardToday(): TodaySnapshot

    // Chat
    @GET("chat/conversations")
    suspend fun getConversations(): List<Conversation>

    @POST("chat/conversations")
    suspend fun createConversation(): Conversation

    @GET("chat/conversations/{id}/messages")
    suspend fun getMessages(@Path("id") id: Long): List<ChatMessageDto>

    @POST("chat/conversations/{id}/messages")
    suspend fun sendMessage(@Path("id") id: Long, @Body req: SendMessageRequest): ChatMessageDto
}
