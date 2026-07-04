package com.NutriApp.NutriApp.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "hidratacion")
public class Hidratacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cantidadMl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dia_id", nullable = false) // Esto obliga a que la FK no sea nula
    private Dia dia;
}
