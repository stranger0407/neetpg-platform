package com.neetpg.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chapters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
