package com.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CD extends Product {

    @Column(nullable = true, length = 255)
    private String artist;

    @Column(nullable = true, length = 100)
    private String genre;

    @Column(name = "record_label", nullable = true, length = 100)
    private String recordLabel;

    @Column(length = 255)
    private String studio;

    @Column(name = "track_list", columnDefinition = "TEXT", nullable = true)
    private String trackList;

    @Column(name = "disc_type", nullable = true, length = 50)
    private String discType;

    @Column(name = "release_date", nullable = true)
    private LocalDate releaseDate;
}
