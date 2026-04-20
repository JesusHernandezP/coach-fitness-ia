package com.fitnesscoach.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class GoalDeserializationTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void deserializesLoseFromAngularValue() throws Exception {
    Goal goal = mapper.readValue("\"LOSE\"", Goal.class);
    assertThat(goal).isEqualTo(Goal.LOSE_WEIGHT);
  }

  @Test
  void deserializesGainFromAngularValue() throws Exception {
    Goal goal = mapper.readValue("\"GAIN\"", Goal.class);
    assertThat(goal).isEqualTo(Goal.GAIN_WEIGHT);
  }

  @Test
  void deserializesMaintainUnchanged() throws Exception {
    Goal goal = mapper.readValue("\"MAINTAIN\"", Goal.class);
    assertThat(goal).isEqualTo(Goal.MAINTAIN);
  }

  @Test
  void serializesLoseWeightAsLose() throws Exception {
    String json = mapper.writeValueAsString(Goal.LOSE_WEIGHT);
    assertThat(json).isEqualTo("\"LOSE\"");
  }

  @Test
  void serializesGainWeightAsGain() throws Exception {
    String json = mapper.writeValueAsString(Goal.GAIN_WEIGHT);
    assertThat(json).isEqualTo("\"GAIN\"");
  }
}
