package com.example.universityservice.command.entity;

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

import java.io.Serializable;

@Entity
@Table(name = "major_subject_combinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MajorSubjectCombination implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Id
    @ManyToOne
    @JoinColumn(name = "subject_combination_id", nullable = false)
    private SubjectCombination subjectCombination;
}