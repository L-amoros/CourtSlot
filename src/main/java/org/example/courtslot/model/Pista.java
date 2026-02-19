package org.example.courtslot.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "pistas")
public class Pista {

    // ── Enum de estado ────────────────────────────────────────────────────────
    public enum Estado {
        ACTIVA, MANTENIMIENTO, DESACTIVADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Double precioPorHora;

    @Column(length = 255)
    private String imagenUrl;

    // Reemplaza el Boolean activa por el enum Estado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.ACTIVA;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deporte_id", nullable = false)
    private Deporte deporte;

    @OneToMany(mappedBy = "pista", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas;

    // ── Constructores ─────────────────────────────────────────────────────────
    public Pista() {}

    public Pista(String nombre, String descripcion, Double precioPorHora,
                 String imagenUrl, Deporte deporte) {
        this.nombre        = nombre;
        this.descripcion   = descripcion;
        this.precioPorHora = precioPorHora;
        this.imagenUrl     = imagenUrl;
        this.deporte       = deporte;
        this.estado        = Estado.ACTIVA;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public Long getId()                             { return id; }
    public void setId(Long id)                      { this.id = id; }
    public String getNombre()                       { return nombre; }
    public void setNombre(String nombre)            { this.nombre = nombre; }
    public String getDescripcion()                  { return descripcion; }
    public void setDescripcion(String d)            { this.descripcion = d; }
    public Double getPrecioPorHora()                { return precioPorHora; }
    public void setPrecioPorHora(Double p)          { this.precioPorHora = p; }
    public String getImagenUrl()                    { return imagenUrl; }
    public void setImagenUrl(String imagenUrl)      { this.imagenUrl = imagenUrl; }
    public Deporte getDeporte()                     { return deporte; }
    public void setDeporte(Deporte deporte)         { this.deporte = deporte; }
    public List<Reserva> getReservas()              { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }

    public Estado getEstado()                       { return estado; }
    public void setEstado(Estado estado)            { this.estado = estado; }

    // Compatibilidad con código que usaba getActiva()
    public Boolean getActiva() { return estado == Estado.ACTIVA; }
    public void setActiva(Boolean ACTIVA) {
        this.estado = ACTIVA ? Estado.ACTIVA : Estado.DESACTIVADA;
    }

    @Override
    public String toString() {
        return "Pista{id=" + id + ", nombre='" + nombre + "', estado=" + estado + "}";
    }
}
