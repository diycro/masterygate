package com.studio.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/** One "the user was active on this day" marker — the basis for a real (not fabricated) streak. */
@Entity
@Table(name = "activity", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "activity_day"}))
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(name = "activity_day")   // "day" is a reserved word in H2/Postgres
    private LocalDate day;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }
}
