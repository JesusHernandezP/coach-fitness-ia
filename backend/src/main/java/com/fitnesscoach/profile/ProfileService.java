package com.fitnesscoach.profile;

import com.fitnesscoach.user.User;
import com.fitnesscoach.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final MetabolicProfileRepository profileRepo;
  private final NutritionTargetRepository targetRepo;
  private final NutritionCalculatorService calculator;
  private final UserRepository userRepo;

  public ProfileResponse getProfile(Long userId) {
    return ProfileResponse.from(findOrThrow(userId));
  }

  @Transactional
  public ProfileResponse saveProfile(Long userId, ProfileRequest req) {
    User user = userRepo.getReferenceById(userId);
    MetabolicProfile profile =
        profileRepo
            .findByUserId(userId)
            .orElse(MetabolicProfile.builder().user(user).build());

    profile.setAge(req.age());
    profile.setSex(req.sex());
    profile.setHeightCm(req.heightCm());
    profile.setCurrentWeightKg(req.currentWeightKg());
    profile.setActivityLevel(req.activityLevel());
    profile.setWeeklyExerciseDays(req.weeklyExerciseDays());
    profile.setExerciseType(req.exerciseType());
    profile.setExerciseMinutes(req.exerciseMinutes());
    profile.setDailySteps(req.dailySteps());
    profile.setDietType(req.dietType());
    profile.setGoal(req.goal());

    profileRepo.save(profile);
    recalculateTargets(user, profile);
    return ProfileResponse.from(profile);
  }

  public NutritionTargetResponse getTargets(Long userId) {
    return NutritionTargetResponse.from(
        targetRepo
            .findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("Perfil no configurado aun")));
  }

  public void recalculateTargets(User user, MetabolicProfile profile) {
    NutritionCalculatorService.NutritionResult result = calculator.calculate(profile);
    NutritionTarget target =
        targetRepo.findByUserId(user.getId()).orElse(NutritionTarget.builder().user(user).build());
    target.setCalories(result.calories());
    target.setProteinG(result.proteinG());
    target.setCarbsG(result.carbsG());
    target.setFatG(result.fatG());
    target.setCalculatedAt(Instant.now());
    targetRepo.save(target);
  }

  private MetabolicProfile findOrThrow(Long userId) {
    return profileRepo
        .findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado"));
  }
}
