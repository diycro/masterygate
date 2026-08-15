package com.studio.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One interview-prep Q&A card for a module: a real question, its full answer, and an explanation of
 * why that's the right answer / what an interviewer is probing for. Persisted so the "generate more"
 * feature can permanently append to the bank over time — it only ever grows.
 */
@Entity
@Table(name = "interview_qa")
public class InterviewQAEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String moduleId;

    @Lob
    private String question;

    @Lob
    private String answer;

    @Lob
    private String explanation;

    /** "high" | "medium" — how frequently this type of question comes up in real interviews. */
    private String frequency;

    /** "curated" | "generated" — curated = hand-vetted seed content, generated = LLM-appended. */
    private String source;

    private Instant createdAt;

    public Long getId() { return id; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
