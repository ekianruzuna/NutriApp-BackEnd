package com.NutriApp.NutriApp.modelo;

import com.NutriApp.NutriApp.modelo.enums.EstadoDia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Email;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "dia")
public class Dia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = false, foreignKey = @ForeignKey(name = "fk_dia_usuario"))
    @JsonIgnore  // Para que no se arme ciclo infinito al hacer toString o JSON
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;


    private double caloriasRestantes;

    @Enumerated(EnumType.STRING)
    private EstadoDia estadoDia;

    @OneToMany(mappedBy = "dia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComidaIngerida> comidasIngeridas = new ArrayList<>();

    @JsonProperty("username")  //le estamos diciendo que cuando agararre un json de este objeto tambien tome este como atributo, ya que el usuario lo ignora con el @JsonIgnore
    public String getUsername(){
        return usuario.getUsername();
    }

    @OneToMany(mappedBy = "dia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActividadFisica> actividadesFisicasRealizadas = new ArrayList<>();

    @OneToOne(mappedBy = "dia", cascade = CascadeType.ALL, orphanRemoval = true)
    private Hidratacion hidratacion = new Hidratacion();
}
