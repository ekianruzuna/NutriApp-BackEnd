package com.NutriApp.NutriApp.modelo.Logros;

import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "logro")
public class Logro {

    @Enumerated(EnumType.STRING)    //necesario para que lo maneje como string, sino lo maneja como numeros
    @Id
    private TipoLogro tipoLogro;

    private String descripcion;


}
