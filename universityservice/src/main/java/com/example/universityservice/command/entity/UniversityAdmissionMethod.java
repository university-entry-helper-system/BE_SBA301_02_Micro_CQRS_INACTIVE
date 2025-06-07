package com.example.universityservice.command.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable; // Cần thiết cho Composite PK

@Entity
@Table(name = "university_admission_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityAdmissionMethod implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Id
    @ManyToOne
    @JoinColumn(name = "admission_method_id", nullable = false)
    private AdmissionMethod admissionMethod;

    @Id
    @Column(name = "year", nullable = false)
    private Integer year; // The year this method is applied by the university

    @Column(name = "quota_percentage")
    private Float quotaPercentage;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}