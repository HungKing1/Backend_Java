package com.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book extends Product {

    @Column(nullable = true, length = 255)
    private String author;

    @Column(nullable = true, length = 255)
    private String publisher;

    @Column(name = "publication_date", nullable = true)
    private LocalDate publicationDate;

    @Column(name = "cover_type", nullable = true, length = 50)
    private String coverType;

    @Column(nullable = true)
    private Integer numOfPages;

    @Column(nullable = true, length = 255)
    private String language;

    @Column(nullable = true, length = 255)
    private String genre;
}
