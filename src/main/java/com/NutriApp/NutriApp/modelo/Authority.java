package com.NutriApp.NutriApp.modelo;

import com.NutriApp.NutriApp.modelo.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jdk.jfr.Name;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Entity
@Data
@Table(name = "authorities")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private Role authority;

    @OneToOne (cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn (name = "username", unique = true, foreignKey = @ForeignKey(name = "fk_authority_usuario"))
    @ToString.Exclude //esto hace falta para que no entre en un ciclo infinito cuando se llama al tostring
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Usuario usuario;

    @JsonProperty("username")  //le estamos diciendo que cuando agararre un json de este objeto tambien tome este como atributo, ya que el usuario lo ignora con el @JsonIgnore
    public String getUsername(){
        return usuario.getUsername();
    }

}
