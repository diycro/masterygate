package com.studio.persist;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** A completed mock-interview attempt's summary — enough for a dashboard history, not full transcripts. */
@Entity
@Table(name = "mock_interview_result")
public class MockInterviewResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String focusTopicId;
    private int dsaScore;
    private int systemDesignScore;
    private int deepDiveScore;
    private int overallScore;
    private Instant completedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFocusTopicId() { return focusTopicId; }
    public void setFocusTopicId(String focusTopicId) { this.focusTopicId = focusTopicId; }
    public int getDsaScore() { return dsaScore; }
    public void setDsaScore(int dsaScore) { this.dsaScore = dsaScore; }
    public int getSystemDesignScore() { return systemDesignScore; }
    public void setSystemDesignScore(int systemDesignScore) { this.systemDesignScore = systemDesignScore; }
    public int getDeepDiveScore() { return deepDiveScore; }
    public void setDeepDiveScore(int deepDiveScore) { this.deepDiveScore = deepDiveScore; }
    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
