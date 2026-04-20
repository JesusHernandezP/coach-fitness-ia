package com.fitnesscoach.profile;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Goal {
  @JsonAlias("LOSE")
  LOSE_WEIGHT("LOSE"),
  MAINTAIN("MAINTAIN"),
  @JsonAlias("GAIN")
  GAIN_WEIGHT("GAIN");

  private final String value;

  Goal(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
