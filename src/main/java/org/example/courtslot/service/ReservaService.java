package org.example.courtslot.service;

import org.example.courtslot.dao.ReservaDAO;
import org.example.courtslot.model.Pista;
import org.example.courtslot.model.Reserva;
import org.example.courtslot.model.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservaService {

    private final ReservaDAO reservaDAO;

    public ReservaService() {
        this.reservaDAO = new ReservaDAO();
    }

    public ReservaService(ReservaDAO reservaDAO) {
        this.reservaDAO = reservaDAO;
    }

    public Reserva crear(Usuario usuario, Pista pista, LocalDate fecha,
                         LocalTime horaInicio, LocalTime horaFin) {
        if (fecha == null)
            throw new IllegalArgumentException("La fecha no puede estar vacía.");
        if (fecha.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("No se pueden hacer reservas en fechas pasadas.");
        if (horaInicio == null || horaFin == null)
            throw new IllegalArgumentException("Debes seleccionar hora de inicio y fin.");
        if (!horaFin.isAfter(horaInicio))
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
        if (pista == null)
            throw new IllegalArgumentException("Debes seleccionar una pista.");
        if (usuario == null)
            throw new IllegalArgumentException("No hay usuario logueado.");
        if (reservaDAO.existeSolapamiento(pista.getId(), fecha, horaInicio, horaFin))
            throw new IllegalArgumentException("Esa pista ya está reservada en ese horario.");

        Reserva reserva = new Reserva(usuario, pista, fecha, horaInicio, horaFin);
        reservaDAO.save(reserva);
        return reserva;
    }

    public void cancelar(Long id) {
        reservaDAO.cancelar(id);
    }

    public List<Reserva> getMisReservas(Long usuarioId) {
        return reservaDAO.findByUsuario(usuarioId);
    }

    public List<Reserva> getAll() {
        return reservaDAO.findAll();
    }

    public List<Reserva> getSlotsBloqueados(Long pistaId, LocalDate fecha) {
        return reservaDAO.findByPistaYFecha(pistaId, fecha);
    }

    public double calcularPrecio(Pista pista, LocalTime horaInicio, LocalTime horaFin) {
        if (pista == null || horaInicio == null || horaFin == null) return 0;
        long horas = java.time.Duration.between(horaInicio, horaFin).toHours();
        return horas * pista.getPrecioPorHora();
    }
    public int contarReservasEnFecha(LocalDate fecha) {
        return reservaDAO.findByFecha(fecha).size();
    }

    public String getFranjaHorariaMasReservada() {
        List<Reserva> todasReservas = reservaDAO.findAll();

        String franjaMaxima = "—";
        int maxContador = 0;

        for (Reserva r : todasReservas) {
            if (r.getEstado() == Reserva.Estado.CANCELADA) continue;

            String franja = r.getHoraInicio().getHour() + "h / " + r.getHoraFin().getHour() + "h";
            int contador = 0;
            for (Reserva r2 : todasReservas) {
                if (r2.getEstado() != Reserva.Estado.CANCELADA) {
                    String franja2 = r2.getHoraInicio().getHour() + "h / " + r2.getHoraFin().getHour() + "h";
                    if (franja.equals(franja2)) contador++;
                }
            }
            if (contador > maxContador) {
                maxContador = contador;
                franjaMaxima = franja;
            }
        }
        return franjaMaxima;
    }
}