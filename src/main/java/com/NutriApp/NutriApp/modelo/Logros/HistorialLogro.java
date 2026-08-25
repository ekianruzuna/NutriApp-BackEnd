package com.NutriApp.NutriApp.modelo.Logros;

import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    //el dia que pertenece el logro
    private LocalDate fechaObtencion;

    //la hora excata a la que se registra el logro (sirve para consultar cual fue el ultimo obtenido)
    private LocalDateTime fechaRegistro;

    //le ponemos esto para que en el json solo agarre el username del usuario y no agarre todo lo demas que tiene
    @JsonProperty("username")
    public String getUsuario() {
        return usuario.getUsername();
    }

    //le ponemos esto para que en el json solo agarre el tipo del logro y no agarre todo lo demas que tiene
    @JsonProperty("logro_id")
    public TipoLogro getLogroObtenido() {
        return logroObtenido.getTipoLogro();
    }
}
