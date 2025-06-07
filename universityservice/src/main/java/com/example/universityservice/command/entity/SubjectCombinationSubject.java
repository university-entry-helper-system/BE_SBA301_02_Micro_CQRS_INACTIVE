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
@Table(name = "subject_combination_subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectCombinationSubject implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "subject_combination_id", nullable = false)
    private SubjectCombination subjectCombination;

    @Id
    @ManyToOne
    @JoinColumn(name = "exam_subject_id", nullable = false)
    private ExamSubject examSubject;
}