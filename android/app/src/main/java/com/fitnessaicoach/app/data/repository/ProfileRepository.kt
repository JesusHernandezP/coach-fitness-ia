package com.fitnessaicoach.app.data.repository

import com.fitnessaicoach.app.data.network.ApiService
import com.fitnessaicoach.app.data.network.MetabolicProfile
import com.fitnessaicoach.app.data.network.NutritionTarget
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val api: ApiService) {

    suspend fun getProfile(): Result<MetabolicProfile>  = runCatching { api.getProfile() }

    suspend fun saveProfile(profile: MetabolicProfile): Result<MetabolicProfile> =
        runCatching { api.saveProfile(profile) }

    suspend fun getTargets(): Result<NutritionTarget> = runCatching { api.getTargets() }
}
