package com.example.universityservice.command.entity;

import com.example.universityservice.BaseEntity;
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

@Entity
@Table(name = "admission_benchmarks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"university_id", "major_id", "year", "admission_method_id", "subject_combination_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionBenchmark extends BaseEntity {
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
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "admission_method_id", nullable = false)
    private AdmissionMethod admissionMethod;

    @ManyToOne
    @JoinColumn(name = "subject_combination_id")
    private SubjectCombination subjectCombination;

    @Column(name = "base_score")
    private Float baseScore;

    @Column(name = "priority_points", nullable = false)
    private Float priorityPoints;

    @Column(name = "total_score", nullable = false)
    private Float totalScore; // base_score + priority_points

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "specific_method_notes", columnDefinition = "TEXT")
    private String specificMethodNotes;
}