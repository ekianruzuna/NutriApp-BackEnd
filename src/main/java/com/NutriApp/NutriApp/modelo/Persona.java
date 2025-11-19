package com.NutriApp.NutriApp.modelo;

import com.NutriApp.NutriApp.modelo.dto.PersonaDTO;
import com.NutriApp.NutriApp.modelo.enums.Genero;
import com.NutriApp.NutriApp.modelo.enums.NivelActividadFisica;
import com.NutriApp.NutriApp.modelo.enums.ObjetivoCaloricoTipo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.Tolerate;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "persona")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El nombre no debe contener números ni caracteres especiales")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 3, max = 50, message = "El apellido debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El apellido no debe contener números ni caracteres especiales")
    private String apellido;

    @NotBlank
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 digitos numericos")
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos numéricos")
    @Column(unique = true)
    private String dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{6,15}", message = "El teléfono debe contener entre 6 y 15 dígitos")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\s\\-ºª#.,]+$",
            message = "La dirección debe contener letras y al menos un número"
    )
    private String direccion;

    @NotNull(message = "El género es obligatorio")
    private Genero genero;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido y contener '@'")
    private String email;

    // Es recomendable modelar las relaciones en ambos lados (bidireccionales) si se necesita
    // acceder a ambas entidades desde el código, no solo desde la base de datos.
    // Al usar JPA/Hibernate, modelar ambos lados permite navegar desde una entidad a otra,
    // y mantener sincronizadas las asociaciones en memoria.
    // El uso de 'mappedBy' indica que esta clase es de la que va a depender la otra clase relacionada.
    // EL uso del cascade para que se maneje en cascada el con el cascade = TIPOCASCADE
    // y el orphanRemoval (este es para el OnDeleteCascade de mysql)
    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private Usuario usuario;

    @JsonProperty("username")
    public String getUsername(){
        return usuario != null ? usuario.getUsername() : null;
    }

    @Tolerate
    public Persona(String nombre, String apellido, String dni, LocalDate fechaNacimiento, String telefono, String direccion, Genero genero, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.direccion = direccion;
        this.genero = genero;
        this.email = email;
    }

    //Transforma a un dto
    public PersonaDTO toDTO(){
        return new PersonaDTO(
                nombre,
                apellido,
                dni,
                fechaNacimiento,
                telefono,
                direccion,
                genero,
                email
        );
    }

}
