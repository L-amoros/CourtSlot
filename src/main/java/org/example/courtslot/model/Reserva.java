package org.example.courtslot.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservas")
public class Reserva {

    public enum Estado { PENDIENTE, CONFIRMADA, CANCELADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pista_id", nullable = false)
    private Pista pista;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Double precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Estado estado;

    public Reserva() {}

    public Reserva(Usuario usuario, Pista pista, LocalDate fecha,
                   LocalTime horaInicio, LocalTime horaFin) {
        this.usuario    = usuario;
        this.pista      = pista;
        this.fecha      = fecha;
        this.horaInicio = horaInicio;
        this.horaFin    = horaFin;
        this.estado     = Estado.CONFIRMADA;
        long horas = java.time.Duration.between(horaInicio, horaFin).toHours();
        this.precioTotal = horas * pista.getPrecioPorHora();
    }

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public Usuario getUsuario()                { return usuario; }
    public void setUsuario(Usuario u)          { this.usuario = u; }
    public Pista getPista()                    { return pista; }
    public void setPista(Pista p)              { this.pista = p; }
    public LocalDate getFecha()                { return fecha; }
    public void setFecha(LocalDate fecha)      { this.fecha = fecha; }
    public LocalTime getHoraInicio()           { return horaInicio; }
    public void setHoraInicio(LocalTime h)     { this.horaInicio = h; }
    public LocalTime getHoraFin()              { return horaFin; }
    public void setHoraFin(LocalTime h)        { this.horaFin = h; }
    public Double getPrecioTotal()             { return precioTotal; }
    public void setPrecioTotal(Double p)       { this.precioTotal = p; }
    public Estado getEstado()                  { return estado; }
    public void setEstado(Estado estado)       { this.estado = estado; }
}
