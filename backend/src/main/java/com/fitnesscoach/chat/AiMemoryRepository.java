package com.fitnesscoach.chat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AiMemoryRepository {

  private final JdbcTemplate jdbcTemplate;

  public void save(
      Long userId, AiMemoryType type, String content, String embedding, int importance) {
    jdbcTemplate.update(
        """
        INSERT INTO ai_memories (user_id, type, content, embedding, importance, created_at)
        VALUES (?, ?, ?, CAST(? AS vector), ?, NOW())
        """,
        userId,
        type.name(),
        content,
        embedding,
        importance);
  }

  public List<AiMemory> findForUser(Long userId) {
    return jdbcTemplate.query(
        """
        SELECT id, user_id, type, content, embedding::text AS embedding, importance, created_at
        FROM ai_memories
        WHERE user_id = ? OR (user_id IS NULL AND type = 'system_knowledge')
        ORDER BY importance DESC, created_at DESC
        """,
        this::mapRow,
        userId);
  }

  public boolean exists(Long userId, AiMemoryType type, String content) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM ai_memories
            WHERE user_id = ? AND type = ? AND LOWER(content) = LOWER(?)
            """,
            Integer.class,
            userId,
            type.name(),
            content);
    return count != null && count > 0;
  }

  private AiMemory mapRow(ResultSet rs, int rowNum) throws SQLException {
    Timestamp createdAt = rs.getTimestamp("created_at");
    return new AiMemory(
        rs.getLong("id"),
        rs.getObject("user_id", Long.class),
        AiMemoryType.valueOf(rs.getString("type")),
        rs.getString("content"),
        rs.getString("embedding"),
        rs.getInt("importance"),
        createdAt != null ? createdAt.toInstant() : null);
  }
}
