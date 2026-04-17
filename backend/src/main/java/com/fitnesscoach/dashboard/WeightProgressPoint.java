package com.fitnesscoach.dashboard;

import java.time.Instant;

public record WeightProgressPoint(Instant loggedAt, Double weightKg) {}
