package com.NutriApp.NutriApp.modelo;

import com.NutriApp.NutriApp.modelo.dto.PersonaDTO;
import com.NutriApp.NutriApp.modelo.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class Usuario implements UserDetails {

    @Id
    @Column(length = 50)
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres")
    @Pattern(regexp = ".*\\D.*", message = "El nombre de usuario no puede contener solo números")
    @Pattern(regexp = ".*[a-zA-Z0-9].*", message = "El nombre de usuario debe contener al menos una letra o número")
    @Pattern(regexp = "\\S+", message = "El nombre de usuario no debe contener espacios")
    @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "El nombre de usuario solo puede contener letras, números, puntos y guiones bajos")
    @Pattern(regexp = "^(?![._])[a-zA-Z0-9._]+(?<![._])$", message = "El nombre de usuario no puede comenzar ni terminar con punto o guion bajo")
    @Pattern(regexp = "^(?!.*[._]{2})[a-zA-Z0-9._]+$", message = "El nombre de usuario no puede contener caracteres especiales consecutivos")
    private String username;  // puede ser email


    @Size(min = 6, max = 64) //max = 64 porque cuando se encripta es muy larga
    @JsonIgnore
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-\\=\\[\\]{};':\"\\\\|,.<>/?]).+$",
            message = "La contraseña debe contener al menos una letra, un número y un carácter especial"
    )
    private String password;

    private boolean enabled; // este campo es obligatorio en tu tabla SQL

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude //esto hace falta para que no entre en un ciclo infinito cuando se llama al tostring
    @JsonIgnore
    private Authority role;

    @OneToOne (cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_usuario_persona"))
    @ToString.Exclude //esto hace falta para que no entre en un ciclo infinito cuando se llama al tostring
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Persona persona;

    @OneToOne (cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "perfilNutricional_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_usuario_perfilNutricional"))
    @ToString.Exclude //esto hace falta para que no entre en un ciclo infinito cuando se llama al tostring
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private PerfilNutricional perfilNutricional;

    @OneToMany (mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude //esto hace falta para que no entre en un ciclo infinito cuando se llama al tostring
    @JsonIgnore
    private List<Dia> dias = new ArrayList<>();

    @OneToMany (mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude //esto hace falta para que no entre en un ciclo infinito cuando se llama al tostring
    private List<ComidaFavorita> comidaFavoritas = new ArrayList<>();



    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SolicitudAltaAlimento> solicitudes = new ArrayList<>();


    private LocalDate fechaActiva = LocalDate.now();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority().toString()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonProperty ("role")  //como en authoritie tenemos un json ignore para que no entre en ciclo infinito, aca le pongo esta anotacion que le dice que agarre el atributo role y haga un getter del rol
    public String getRole() {
        return role.getAuthority().toString();
    }   //basicamente le esta diciendo cuando toma un json del objeto Usuario que solo tome el atributo role para no entrar en ciclos infinitos


    @JsonProperty ("nombre")
    public String getNombrePersona (){
        return persona.getNombre();
    }
}
