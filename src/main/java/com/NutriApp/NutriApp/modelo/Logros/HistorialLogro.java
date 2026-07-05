package com.NutriApp.NutriApp.modelo.Logros;

import com.NutriApp.NutriApp.modelo.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historialLogro")
//            EVENTOSSSS
public class HistorialLogro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id",    //le decimos cual es la columna de la bdd que guarda la relacion
            nullable = false,   //no puede existir un evento sin un usuario
            unique = false,     //puede haber varios eventos por un mismo usuario
            foreignKey = @ForeignKey(name = "fk_usuario_id"))    //con esto solo el ponemos un nombre lindo a la FK en la BDD
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "logro_id", nullable = false, unique = false)
    private Logro logroObtenido;

    private LocalDate fechaObtencion;
}
