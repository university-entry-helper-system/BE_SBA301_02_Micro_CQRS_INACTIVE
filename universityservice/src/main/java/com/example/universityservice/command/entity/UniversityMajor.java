package com.example.universityservice.command.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal; // Cho DECIMAL(18,2)

@Entity
@Table(name = "university_majors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"university_id", "major_id", "year"}) // Unique constraint
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityMajor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "year", nullable = false)
    private Integer year; // Academic year

    @Column(name = "tuition", precision = 18, scale = 2) // DECIMAL(18,2)
    private BigDecimal tuition;

    @Column(name = "quota")
    private Integer quota;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}