package com.fitnesscoach.coach;

import java.time.LocalDate;
import java.util.List;

public record WeeklyReviewResponse(
    LocalDate periodStart,
    LocalDate periodEnd,
    String summary,
    List<String> nutritionFindings,
    List<String> activityFindings,
    List<String> weightFindings,
    List<String> recommendations,
    List<String> riskNotes) {}
