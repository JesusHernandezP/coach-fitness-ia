package com.fitnesscoach.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

class PendingAiActionMappingTest {

  @Test
  void payloadFieldIsMappedAsJson() throws NoSuchFieldException {
    Field field = PendingAiAction.class.getDeclaredField("payload");

    JdbcTypeCode jdbcTypeCode = field.getAnnotation(JdbcTypeCode.class);

    assertThat(jdbcTypeCode).isNotNull();
    assertThat(jdbcTypeCode.value()).isEqualTo(SqlTypes.JSON);
  }
}
