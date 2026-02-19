package org.example.courtslot.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "deportes")
public class Deporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 255)
    private String icono; // nombre del fichero de imagen, ej: "tenis.png"

    @OneToMany(mappedBy = "deporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pista> pistas;

    public Deporte() {}

    public Deporte(String nombre, String descripcion, String icono) {
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.icono       = icono;
    }

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }
    public String getNombre()                   { return nombre; }
    public void setNombre(String nombre)        { this.nombre = nombre; }
    public String getDescripcion()              { return descripcion; }
    public void setDescripcion(String d)        { this.descripcion = d; }
    public String getIcono()                    { return icono; }
    public void setIcono(String icono)          { this.icono = icono; }
    public List<Pista> getPistas()              { return pistas; }
    public void setPistas(List<Pista> pistas)   { this.pistas = pistas; }

    @Override
    public String toString() {
        return "Deporte{id=" + id + ", nombre='" + nombre + "'}";
    }
}
