package com.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "newspapers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Newspaper extends Product {

    @Column(nullable = true, length = 255)
    private String publisher;

    @Column(name = "editor_in_chief", nullable = true, length = 255)
    private String editorInChief;

    @Column(name = "publication_date", nullable = true)
    private LocalDate publicationDate;

    @Column(nullable = true)
    private Integer issueNumber;

    @Column(nullable = true)
    private LocalDate frequency;
}
