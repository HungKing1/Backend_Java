package com.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dvds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DVD extends Product {
    @Column(nullable = true, length = 255)
    private String director;

    @Column(nullable = true, length = 100)
    private String studio;

    @Column(nullable = true)
    private Integer runtime;

    @Column(nullable = true, length = 50)
    private String language;

    @Column(length = 100)
    private String subtitles;

    @Column(name = "disc_type", nullable = true, length = 50)
    private String discType;
}
